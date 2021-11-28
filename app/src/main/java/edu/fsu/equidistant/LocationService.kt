package edu.fsu.equidistant

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Math.abs
import java.util.concurrent.TimeUnit


class LocationService : Service() {

    private var configurationChange = false
    private var serviceRunningInForground = false
    private val localBinder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private val db = Firebase.firestore
    private lateinit var user: FirebaseUser


    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you
    // should receive updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    private var currentLocation: Location? = null

    override fun onCreate() {
        Log.d(TAG,"onCreate()")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply{

            interval = TimeUnit.SECONDS.toMillis(60)

            fastestInterval = TimeUnit.SECONDS.toMillis(30)

            maxWaitTime = TimeUnit.MINUTES.toMillis(2)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object: LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult){
                super.onLocationResult(locationResult)
                if (currentLocation == null) {
                    currentLocation = locationResult.lastLocation
                    val geopoint = onLocationChanged(currentLocation!!)
                    val userRef = db.collection("users").document(user.email!!)
                    val data = hashMapOf("location" to geopoint)
                    userRef.set(data, SetOptions.merge())
                }
                else if (signifigantChange(locationResult.lastLocation)) {
                    currentLocation = locationResult.lastLocation
                    val geopoint = onLocationChanged(currentLocation!!)
                    val userRef = db.collection("users").document(user.email!!)
                    val data = hashMapOf("location" to geopoint)
                    userRef.set(data, SetOptions.merge())
                    Log.d("LOCATIONCALLBACK","Wrote new geopoint to firestore")
                }
                else Log.d("LOCATIONCALLBACK","Change not signifigant, did not write to firestore")
                val intent = Intent(ACTION_LOCATION_SERVICE_BROADCAST)
                intent.putExtra(EXTRA_LOCATION,currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                //update notification content if service is running in foreground
                if (serviceRunningInForground){
                    notificationManager.notify(
                        NOTIFICATION_ID,generateNotification(currentLocation)
                    )
                }
            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
                Log.d("onLocationAvailability","LOCATION AVAILABLE")

            }
        }
    }

    override fun onStartCommand(intent: Intent,flags:Int,startId:Int):Int{
        Log.d(TAG,"onStartCommand")
        user = FirebaseAuth.getInstance().currentUser!!

        val cancelLocationTrackingFromNotification = intent.getBooleanExtra(
            EXTRA_CANCEL_LOCATION_TRAKING_FROM_NOTIFICATION,false)

        if (cancelLocationTrackingFromNotification){
            unsubscribeToLocationUpdates()
            stopSelf()
        }
        //tell system not to recreate service after killed
        return START_NOT_STICKY
    }

    val CHANNEL_ID = "LOCATION_SERVICE_CHANNEL"

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG,"onBind()")

        //MainActivity comes into foreground and binds to service, so service can become background service
        stopForeground(true)
        serviceRunningInForground=false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent){
        Log.d(TAG,"onRebind()")

        // Main Activity comes to foreground
            stopForeground(true)
            serviceRunningInForground=false
            configurationChange=false
            super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent):Boolean{
        Log.d(TAG,"onUnbind()")

        //MainActivity leaves foreground, service becomes foreground service again
        if (!configurationChange && SharedPreferenceUtil.getLocationTrackingPref(this)){
            Log.d(TAG,"start foreground service")
            val notification = generateNotification(currentLocation)
            startForeground(NOTIFICATION_ID,notification)
            serviceRunningInForground=true
        }
        return true
        }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    fun subscribeToLocationUpdates(){
        Log.d(TAG,"subscribeToLocationUpdates()")

        SharedPreferenceUtil.saveLocationTrackingPref(this,true)

        //ensure the service actually starts
        startService(Intent(applicationContext, LocationService::class.java))

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper())
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    fun unsubscribeToLocationUpdates() {
        Log.d(TAG, "unsubscribeToLocationUpdates()")

        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Location Callback removed.")
                    stopSelf()
                } else {
                    Log.d(TAG, "Failed to remove Location Callback.")
                }
            }
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, true)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    inner class LocalBinder: Binder(){
        internal val service: LocationService
        get() = this@LocationService
    }

    private fun generateNotification(location:Location?): Notification {
        Log.d(TAG,"generateNotification()")

        //get data
        val mainNotificationText = location.toString() ?: "No location"
        val titleText = getString(R.string.app_name)

        //create notification channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

            //Build the Big_Text_Style
            val bigTextStyle = NotificationCompat.BigTextStyle()
                .bigText(mainNotificationText)
                .setBigContentTitle(titleText)

            // Set up main Intent/Pending intents for notification
            val launchActivityIntent = Intent(this,MainActivity::class.java)
            val cancelIntent = Intent(this,LocationService::class.java)
            cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRAKING_FROM_NOTIFICATION,true)

            val servicePendingIntent = PendingIntent.getService(this,0,cancelIntent, PendingIntent.FLAG_IMMUTABLE)

            val activityPendingIntent = PendingIntent.getActivity(this,0,launchActivityIntent,PendingIntent.FLAG_IMMUTABLE)

            //Build and issue the notification

            val notificationCompatBuilder =
                NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_launcher_foreground,"Launch Activity",activityPendingIntent)
            .addAction(R.drawable.ic_cancel,"Stop Location Updates",servicePendingIntent)
            .build()
    }

    companion object{
        private const val TAG ="LocationService"
        private const val PACKAGE_NAME="edu.fsu.equidistant"
        internal const val ACTION_LOCATION_SERVICE_BROADCAST="$PACKAGE_NAME.action.LOCATION_SERVICE_BROADCAST"
        internal const val EXTRA_LOCATION ="$PACKAGE_NAME.extra.LOCATION"
        private const val EXTRA_CANCEL_LOCATION_TRAKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"
        private const val NOTIFICATION_ID = 13572468
        private const val NOTIFICATION_CHANNEL_ID ="Location Service Channel"
    }

    private fun onLocationChanged(location: Location):GeoPoint {
        val point = GeoPoint(location.latitude, location.longitude)
        return point
    }

    private fun signifigantChange(location:Location):Boolean{
        val d_lat = abs(currentLocation!!.latitude - location.latitude)
        val d_long = abs(currentLocation!!.longitude - location.longitude)

        return d_lat >= .05 || d_long >=.05
    }

}