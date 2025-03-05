package com.example.storeai.ui.home

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.FileProvider
import com.example.storeai.R
import com.example.storeai.adapters.HomeViewpagerAdapter
import com.example.storeai.adapters.ProductAdapter
import com.example.storeai.data.model.Category
import com.example.storeai.data.model.Product
import com.example.storeai.data.repository.ProductRepository
import com.example.storeai.databinding.FragmentHomeBinding
import com.example.storeai.ui.categories.BaseCategoryFragment
import com.example.storeai.ui.categories.MainCategoryFragment
import com.example.storeai.utils.toTempFile
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel by viewModels<HomeViewModel>()

    // Request codes
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val REQUEST_CODE_PERMISSIONS = 101

    // Permissions
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private var currentImageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            setupViewPager(categories)
        }

        binding.searchBar.setOnClickListener {
            // Handle search
        }

        binding.ivScan.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun setupViewPager(categories: List<Category>) {
        val fragments = mutableListOf<Fragment>().apply {
            add(MainCategoryFragment())
            categories.forEach { category ->
                add(BaseCategoryFragment.newInstance(category.id))
            }
        }

        val adapter = HomeViewpagerAdapter(
            fragments,
            childFragmentManager,
            lifecycle
        )

        binding.viewpagerHome.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewpagerHome) { tab, position ->
            tab.text = when(position) {
                0 -> "Main"
                else -> categories[position-1].title
            }
        }.attach()
    }

    private fun showImageSourceDialog() {
        if (allPermissionsGranted()) {
            val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
            AlertDialog.Builder(requireContext()).apply {
                setItems(options) { _, which ->
                    when(which) {
                        0 -> takePhoto()
                        1 -> pickFromGallery()
                    }
                }
            }.show()
        } else {
            requestPermissions(
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                currentImageUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> handleImage(currentImageUri)
                REQUEST_IMAGE_PICK -> handleImage(data?.data)
            }
        }
    }

    private fun handleImage(uri: Uri?) {
        uri?.let {
            try {
                val imageFile = it.toTempFile(requireContext())
                viewLifecycleOwner.lifecycleScope.launch {
                    showLoading()
                    try {
                        val similarProducts = ProductRepository().getSimilarProducts(imageFile)
                        showSimilarProducts(similarProducts)
                    } catch (e: Exception) {
                        showError(e.message)
                    }
                    hideLoading()
                }
            } catch (e: Exception) {
                showError("Error processing image: ${e.localizedMessage}")
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                showImageSourceDialog()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showSimilarProducts(products: List<Product>) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_similar_products)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.rvSimilarProducts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ProductAdapter(products) { product ->
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToProductDetailFragment(product.id)
            )
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLoading() {
        // Implement loading indicator
    }

    private fun showError(message: String?) {
        Toast.makeText(requireContext(), message ?: "Unknown error", Toast.LENGTH_LONG).show()
    }

    private fun hideLoading() {
        // Hide loading indicator
    }
}