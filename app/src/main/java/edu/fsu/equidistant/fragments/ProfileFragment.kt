package edu.fsu.equidistant.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentProfileBinding
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL

class ProfileFragment : Fragment(R.layout.fragment_home) {


    private val storage = Firebase.storage
    val storageRef = storage.reference
    private val args: ProfileFragmentArgs by navArgs()
    private var binding: FragmentProfileBinding? = null
    private var user: FirebaseUser? = null
    private var imageUri: Uri? = null
    private val db = Firebase.firestore




    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding!!.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        user = FirebaseAuth.getInstance().currentUser


    /*
        Get image from gallery, sets it as the users profile photo
        and uploads it to firebase storage
     */
        //TODO Clean up filepath and integrate with firestore to link account to photo
        val loadImage=registerForActivityResult(
            GetContent(),
            ActivityResultCallback {
                binding?.imageViewProfilePhoto?.setImageURI(it)
                imageUri = it

                val imageRef = storageRef.child("users/" + user?.email + "/" + imageUri)
                val baos = ByteArrayOutputStream()

                binding?.let { it1 -> getBitmapFromView(it1.imageViewProfilePhoto) }
                    ?.compress(Bitmap.CompressFormat.JPEG, 100, baos)

                val data = baos.toByteArray()
                val uploadTask = imageRef.putBytes(data)

                uploadTask.addOnFailureListener {
                    Toast.makeText(context,it.message,Toast.LENGTH_LONG).show()
                }.addOnSuccessListener { taskSnapshot ->
                    // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                    Toast.makeText(context,"Upload Success",Toast.LENGTH_LONG).show()
                }

                /*
                    Update users profile url
                 */
                val profileUpdates = userProfileChangeRequest {
                    if (imageUri!=null) {
                        photoUri = imageUri
                    }
                }
                //TODO Dont update profile if user canceled uploading a photo
                user!!.updateProfile(profileUpdates)
                    .addOnCompleteListener { task->
                        if (task.isSuccessful){
                            Toast.makeText(context,"Profile Update Successful",Toast.LENGTH_LONG).show()
                            val data = hashMapOf(
                                "name" to user?.email,
                                "avatar" to storageRef.child(user?.photoUrl.toString()).path
                            )

                            user?.email?.let { it1 -> db.collection("users").document(it1).set(data, SetOptions.merge()) }
                        }
                        else{
                            Toast.makeText(context,"Failed to update profile",Toast.LENGTH_LONG).show()
                        }
                    }
            })

        /*
            Set views
         */
        binding!!.apply{

            //TODO Set size of profile photo to some ratio of screen size
            /*
                Download users profile photo from cloud storage
             */
            if(user?.photoUrl != null){
                var imageRef = storageRef.child("users/"+ user?.email + "/" +user?.photoUrl.toString())
                val ONE_MB : Long = 1024 * 1024
                imageRef.getBytes(ONE_MB).addOnSuccessListener {
                    val bmp:Bitmap = BitmapFactory.decodeByteArray(it,0,it.size)
                    //TODO Change to setImageBitmap(Bitmap.createScaledBitmap(...) when image view size is determined
                    imageViewProfilePhoto.setImageBitmap(bmp)
                }.addOnFailureListener{
                    Toast.makeText(context,it.message,Toast.LENGTH_LONG).show()
                }

            }

            /*
                upload photo to firebase storage
             */
            imageViewProfilePhoto.setOnClickListener {
                loadImage.launch("image/*")
            }

            ibBackArrow.setOnClickListener {
                val action = ProfileFragmentDirections.actionProfileFragmentToHomeFragment(
                    user?.email!!,
                    user!!.uid
                )
                findNavController().navigate(action)
            }
            btUpdate.setOnClickListener {
                val action = ProfileFragmentDirections.actionProfileFragmentToHomeFragment(
                    user?.email!!,
                    user!!.uid
                )
                findNavController().navigate(action)

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




}