'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiTag, FiPlus, FiEdit, FiTrash2, FiChevronRight, FiChevronDown } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import { categoryApi } from '@/lib/api'
import EmployeeBreadcrumb from '@/components/EmployeeBreadcrumb'

export default function CategoriesPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  const [categories, setCategories] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editingCategory, setEditingCategory] = useState<any>(null)
  const [expandedCategories, setExpandedCategories] = useState<Set<number>>(new Set())
  const [form, setForm] = useState({
    name: '',
    description: '',
    parentId: null as number | null,
    imageUrl: '',
    displayOrder: 0,
    active: true
  })

  useEffect(() => {
    console.log('🔍 Auth Check:', { isAuthenticated, user })
    
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    console.log('👤 User role:', user?.role)
    
    if (user?.role !== 'PRODUCT_MANAGER' && user?.role !== 'ADMIN') {
      toast.error('Chỉ quản lý sản phẩm mới có quyền truy cập')
      router.push('/')
      return
    }

    loadCategories()
  }, [isAuthenticated, user, router])

  const loadCategories = async () => {
    try {
      const response = await categoryApi.getAll()
      setCategories(response.data || [])
    } catch (error) {
      console.error('Error loading categories:', error)
      toast.error('Lỗi khi tải danh mục')
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = () => {
    setEditingCategory(null)
    setForm({
      name: '',
      description: '',
      parentId: null,
      imageUrl: '',
      displayOrder: 0,
      active: true
    })
    setShowModal(true)
  }

  const handleEdit = (category: any) => {
    setEditingCategory(category)
    setForm({
      name: category.name,
      description: category.description || '',
      parentId: category.parentId || null,
      imageUrl: category.imageUrl || '',
      displayOrder: category.displayOrder || 0,
      active: category.active
    })
    setShowModal(true)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!form.name) {
      toast.error('Vui lòng nhập tên danh mục')
      return
    }

    try {
      console.log('📤 Submitting category:', form)
      
      const response = editingCategory 
        ? await categoryApi.update(editingCategory.id, form)
        : await categoryApi.create(form)

      console.log('📥 Response:', response)

      if (response.success) {
        toast.success(editingCategory ? 'Cập nhật danh mục thành công!' : 'Tạo danh mục thành công!')
        setShowModal(false)
        loadCategories()
      } else {
        toast.error(response.message || 'Có lỗi xảy ra')
      }
    } catch (error: any) {
      console.error('❌ Error saving category:', error)
      toast.error(error.message || 'Lỗi khi lưu danh mục')
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn xóa danh mục này?')) return

    try {
      const response = await categoryApi.delete(id)

      if (response.success) {
        toast.success('Xóa danh mục thành công!')
        loadCategories()
      } else {
        toast.error(response.message || 'Có lỗi xảy ra')
      }
    } catch (error: any) {
      console.error('Error deleting category:', error)
      toast.error(error.message || 'Lỗi khi xóa danh mục')
    }
  }

  const toggleExpand = (id: number) => {
    const newExpanded = new Set(expandedCategories)
    if (newExpanded.has(id)) {
      newExpanded.delete(id)
    } else {
      newExpanded.add(id)
    }
    setExpandedCategories(newExpanded)
  }

  const renderCategory = (category: any, level: number = 0) => {
    const hasChildren = category.children && category.children.length > 0
    const isExpanded = expandedCategories.has(category.id)

    return (
      <div key={category.id}>
        <div 
          className="flex items-center justify-between p-4 hover:bg-gray-50 border-b border-gray-200"
          style={{ paddingLeft: `${level * 2 + 1}rem` }}
        >
          <div className="flex items-center space-x-3 flex-1">
            {hasChildren && (
              <button
                onClick={() => toggleExpand(category.id)}
                className="text-gray-400 hover:text-gray-600"
              >
                {isExpanded ? <FiChevronDown /> : <FiChevronRight />}
              </button>
            )}
            {!hasChildren && <div className="w-4"></div>}
            
            <FiTag className="text-gray-400" />
            
            <div className="flex-1">
              <div className="flex items-center space-x-2">
                <span className="font-medium text-gray-900">{category.name}</span>
                {!category.active && (
                  <span className="px-2 py-1 text-xs bg-gray-100 text-gray-600 rounded">
                    Tạm ngưng
                  </span>
                )}
              </div>
              {category.description && (
                <p className="text-sm text-gray-500 mt-1">{category.description}</p>
              )}
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <button
              onClick={() => handleEdit(category)}
              className="p-2 text-blue-500 hover:bg-blue-50 rounded"
            >
              <FiEdit />
            </button>
            {user?.role === 'ADMIN' && (
              <button
                onClick={() => handleDelete(category.id)}
                className="p-2 text-red-500 hover:bg-red-50 rounded"
              >
                <FiTrash2 />
              </button>
            )}
          </div>
        </div>

        {hasChildren && isExpanded && (
          <div>
            {category.children.map((child: any) => renderCategory(child, level + 1))}
          </div>
        )}
      </div>
    )
  }

  const flattenCategories = (cats: any[]): any[] => {
    let result: any[] = []
    cats.forEach(cat => {
      result.push(cat)
      if (cat.children && cat.children.length > 0) {
        result = result.concat(flattenCategories(cat.children))
      }
    })
    return result
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <EmployeeBreadcrumb items={[{ name: 'Quản lý danh mục' }]} />

        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Quản lý danh mục</h1>
          <button
            onClick={handleCreate}
            className="flex items-center space-x-2 bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition-colors"
          >
            <FiPlus />
            <span>Thêm danh mục</span>
          </button>
        </div>

        {categories.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <FiTag size={64} className="mx-auto text-gray-400 mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Chưa có danh mục nào</h3>
            <p className="text-gray-600 mb-6">Bắt đầu tạo danh mục cho sản phẩm</p>
            <button
              onClick={handleCreate}
              className="bg-red-500 text-white px-6 py-3 rounded-lg hover:bg-red-600 transition-colors"
            >
              Tạo danh mục đầu tiên
            </button>
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            {categories.map(category => renderCategory(category))}
          </div>
        )}

        {/* Modal */}
        {showModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
              <div className="p-6">
                <h2 className="text-2xl font-bold text-gray-900 mb-6">
                  {editingCategory ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới'}
                </h2>
                
                <form onSubmit={handleSubmit}>
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Tên danh mục <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        value={form.name}
                        onChange={(e) => setForm({...form, name: e.target.value})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Mô tả
                      </label>
                      <textarea
                        value={form.description}
                        onChange={(e) => setForm({...form, description: e.target.value})}
                        rows={3}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Danh mục cha
                      </label>
                      <select
                        value={form.parentId || ''}
                        onChange={(e) => setForm({...form, parentId: e.target.value ? Number(e.target.value) : null})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      >
                        <option value="">Không có (danh mục gốc)</option>
                        {flattenCategories(categories)
                          .filter(cat => !editingCategory || cat.id !== editingCategory.id)
                          .map(cat => (
                            <option key={cat.id} value={cat.id}>{cat.name}</option>
                          ))
                        }
                      </select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        URL hình ảnh
                      </label>
                      <input
                        type="text"
                        value={form.imageUrl}
                        onChange={(e) => setForm({...form, imageUrl: e.target.value})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                        placeholder="https://example.com/image.jpg"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Thứ tự hiển thị
                      </label>
                      <input
                        type="number"
                        value={form.displayOrder}
                        onChange={(e) => setForm({...form, displayOrder: Number(e.target.value)})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                        min="0"
                      />
                    </div>

                    <div className="flex items-center">
                      <input
                        type="checkbox"
                        id="active"
                        checked={form.active}
                        onChange={(e) => setForm({...form, active: e.target.checked})}
                        className="w-4 h-4 text-red-500 border-gray-300 rounded focus:ring-red-500"
                      />
                      <label htmlFor="active" className="ml-2 text-sm text-gray-700">
                        Kích hoạt
                      </label>
                    </div>
                  </div>

                  <div className="flex space-x-4 mt-6">
                    <button
                      type="button"
                      onClick={() => setShowModal(false)}
                      className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                      Hủy
                    </button>
                    <button
                      type="submit"
                      className="flex-1 bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition-colors"
                    >
                      {editingCategory ? 'Cập nhật' : 'Tạo mới'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
