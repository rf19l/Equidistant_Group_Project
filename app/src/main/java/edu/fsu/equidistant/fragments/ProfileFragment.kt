package edu.fsu.equidistant.fragments

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.fsu.equidistant.R
import edu.fsu.equidistant.data.Profile
import edu.fsu.equidistant.databinding.FragmentProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


/* photoRef represents the firestore photo, imageUri represents component photo */

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    /* Firebase Variables */
    private val storage = Firebase.storage
    val storageRef = storage.reference
    private var user: FirebaseUser? = null
    private val db = Firebase.firestore

    /* Components */
    private var binding: FragmentProfileBinding? = null
    private var imageUri: Uri? = null
    private lateinit var bitmap:Bitmap
    private lateinit var username:String
    private var profileFetched = false

    /* Data */
    private lateinit var profile: Profile

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding!!.root
        CoroutineScope(Dispatchers.IO).launch {
            getUserProfile(binding!!)
            while(!profileFetched){
                Log.d("TAG","Waiting for profile fetch")
            }
            fetchFirebasePicture()

        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    //    setHasOptionsMenu(true)

        /*
            Set views
         */
        binding!!.apply{

            /* Navigation Components */
            ibBackArrow.setOnClickListener {
                val action = ProfileFragmentDirections.actionProfileFragmentToHomeFragment(
                    FirebaseAuth.getInstance().currentUser?.email!!,
                    FirebaseAuth.getInstance().currentUser!!.uid
                )
                findNavController().navigate(action)
            }
            btUpdate.setOnClickListener {
                if(imageUri.toString() != profile.photoRef) {
                    CoroutineScope(Dispatchers.IO).launch {
                        uploadPicture()
                        updateFirestoreImage()
                        deleteOldPicture()
                    }
                if(profile.username != username){
                    CoroutineScope(Dispatchers.IO).launch{
                        updateFirestoreUsername()
                    }
                }
                }
                val action = ProfileFragmentDirections.actionProfileFragmentToHomeFragment(
                    FirebaseAuth.getInstance().currentUser?.email!!,
                    FirebaseAuth.getInstance().currentUser?.uid!!
                )
                findNavController().navigate(action)
                }

            imageViewProfilePhoto.setOnClickListener {
                selectPhoto.launch("image/*")
            }
        }
    }

    private fun deleteOldPicture() {
        val imageRef = storageRef.child("users/" + FirebaseAuth.getInstance().currentUser?.uid + "/" + Uri.parse(profile.photoRef))
        imageRef.delete().addOnSuccessListener(OnSuccessListener<Void?> {
            Log.d("TAG","Successfully deleted old picture from storage")
            // File deleted successfully
        }).addOnFailureListener(OnFailureListener {
            Log.d("TAG","Error Deleting Old Picture from cloud storage")

            // Uh-oh, an error occurred!
        })
    }

    private fun getUserProfile(binding: FragmentProfileBinding){
        if (profileFetched){
            return
        }
        FirebaseAuth.getInstance().currentUser!!.uid?.let {
            db.collection("users").document(it)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }
                    val data = document!!.data!!
                    var tempUri=""
                    if(data["imageUri"] != null){
                        tempUri = data["imageUri"].toString()
                    }
                    profile = Profile(
                        it,
                        data["username"].toString(),
                        tempUri
                    )
                    binding.apply{
                        etEditUsername.setText(data["username"].toString())
                        username=data["username"].toString()
                    }
                    if(tempUri.isNotEmpty()){
                        /*
                        imageUri = Uri.parse(tempUri)
                        bitmap = MediaStore.Images.Media.getBitmap(context?.getContentResolver(),imageUri )
                        binding.imageViewProfilePhoto.setImageBitmap(resizeBitmap(bitmap,600))
                         */
                        //imageUri = Uri.parse(tempUri)
                    }
                    Log.d(ContentValues.TAG, "GetProfile for: ${data["username"]}")
                    profileFetched=true
                }
        }
    }


    /*
        Options Menu Functions
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.option_logout) {
            FirebaseAuth.getInstance().signOut()
            val action = ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
            findNavController().navigate(action)
        }
        return super.onOptionsItemSelected(item)

    }

    // hide edit profile option while on profile page
    override fun onPrepareOptionsMenu(menu: Menu) {
        val logout: MenuItem = menu.findItem(R.id.option_editProfile)
        logout.setVisible(false)
        super.onPrepareOptionsMenu(menu)
    }

    /*
        Reset binding and user
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        user = null
    }

    /*
        Convert ImageView to a bitmap, used for uploading photo to storage
     */
    private fun getBitmapFromView(view: View): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private val selectPhoto = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {
            if(it != null){
            imageUri = it
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(context?.getContentResolver(), imageUri)
                binding?.imageViewProfilePhoto?.setImageBitmap(resizeBitmap(bitmap,600))
            }
        })

    private fun resizeBitmap(source:Bitmap,maxLength:Int):Bitmap{
        try {
            if(source.height != maxLength || source.width != maxLength){
                val result = Bitmap.createScaledBitmap(source, maxLength,maxLength,false)
                bitmap = result
                return result
            }
            return source
        } catch (e: Exception) {
            bitmap = source
            return source
        }
    }

    private fun uploadPicture(){
        val imageRef = storageRef.child("users/" + FirebaseAuth.getInstance().currentUser?.uid + "/" + imageUri)
        val baos = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnFailureListener {
            Toast.makeText(context,it.message,Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { taskSnapshot ->
            Log.d("TAG","Upload Success!")
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            Toast.makeText(context,"Upload Success",Toast.LENGTH_LONG).show()
        }

    }

    private fun fetchFirebasePicture(){
        while(profile == null){
            Log.d("TAG","waiting for profile fetch")
        }
        if (profile.photoRef == null || profile.photoRef.isEmpty()){
            return
        }
        val imageRef = storageRef.child("users/"+ FirebaseAuth.getInstance().currentUser?.uid + "/" +profile.photoRef)
        val ONE_MB : Long = 1024 * 1024

        imageRef.getBytes(ONE_MB)
            .addOnSuccessListener {
                val bmp:Bitmap = BitmapFactory.decodeByteArray(it,0,it.size)
                binding?.imageViewProfilePhoto?.setImageBitmap(resizeBitmap(bmp,600))

        }.
            addOnFailureListener{
                val ref = db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
                ref
                    .update("imageUri", "")
                    .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                Toast.makeText(context,it.message,Toast.LENGTH_LONG).show()
            }
        }

    private fun updateFirestoreImage(){
        val ref = db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
        ref
            .update("imageUri", imageUri.toString())
            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }
    private fun updateFirestoreUsername(){
        val userRef = db.collection("user").document(profile.uuid)
        userRef
            .update("username",username)
            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }
}