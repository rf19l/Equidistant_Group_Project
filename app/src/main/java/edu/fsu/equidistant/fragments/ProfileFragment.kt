package edu.fsu.equidistant.fragments

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentProfileBinding
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment(R.layout.fragment_home) {


    private val storage = Firebase.storage
    val storageRef = storage.reference
    private val args: ProfileFragmentArgs by navArgs()
    private var binding: FragmentProfileBinding? = null
    private var user: FirebaseUser? = null
    private var imageUri: Uri? = null




    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding!!.root
        return view
    }

//TODO Clean up filepath and integrate with firestore to link account to photo
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        user = FirebaseAuth.getInstance().currentUser
        val loadImage=registerForActivityResult(
            GetContent(),
            ActivityResultCallback {
                binding?.imageViewProfilePhoto?.setImageURI(it)
                imageUri = it
                val imageRef = storageRef.child("users/" + user + "/" + imageUri)
                val baos = ByteArrayOutputStream()
                binding?.let { it1 -> getBitmapFromView(it1.imageViewProfilePhoto) }
                    ?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val uploadTask = imageRef.putBytes(data)
                uploadTask.addOnFailureListener {
                    // Handle unsuccessful upload
                    Toast.makeText(context,it.message,Toast.LENGTH_LONG).show()
                }.addOnSuccessListener { taskSnapshot ->
                    // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                    // ...
                    Toast.makeText(context,"Upload Success",Toast.LENGTH_LONG).show()

                }
            })
        binding!!.apply{
            imageViewProfilePhoto.setOnClickListener {
                loadImage.launch("image/*")

            }

        }



    }

    private fun choosePicture(){

        //TODO Get images from gallery

    }

    /*
        Options Menu
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        user = null
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }




}