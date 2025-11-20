// Cloudinary helper (sẽ dùng sau)

export const uploadToCloudinary = async (file: File): Promise<string> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('upload_preset', 'your_upload_preset') // Tạo trong Cloudinary dashboard
  
  try {
    const response = await fetch(
      `https://api.cloudinary.com/v1_1/your_cloud_name/image/upload`,
      {
        method: 'POST',
        body: formData
      }
    )
    
    const data = await response.json()
    return data.secure_url // https://res.cloudinary.com/...
  } catch (error) {
    console.error('Cloudinary upload error:', error)
    throw error
  }
}

// Helper để chuyển đổi URL
export const getImageUrl = (url: string): string => {
  // Nếu là local image
  if (url.startsWith('/images/')) {
    return url
  }
  
  // Nếu là Cloudinary URL
  if (url.startsWith('https://res.cloudinary.com/')) {
    return url
  }
  
  // Fallback
  return url
}
