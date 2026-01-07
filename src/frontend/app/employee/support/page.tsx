'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'
import { supportApi } from '@/lib/api'
import toast from 'react-hot-toast'
import {
  FiMessageCircle, FiSearch, FiFilter, FiClock, FiCheckCircle,
  FiAlertCircle, FiUser, FiChevronRight, FiRefreshCw, FiX
} from 'react-icons/fi'

interface Ticket {
  id: string | number
  title: string
  status: string
  priority: string
  category: string
  createdAt: string
  updatedAt: string
  customerName?: string
  customerEmail?: string
}

const statusConfig: Record<string, { label: string; color: string }> = {
  open: { label: 'Đang mở', color: 'bg-blue-100 text-blue-800' },
  pending: { label: 'Chờ xử lý', color: 'bg-yellow-100 text-yellow-800' },
  in_progress: { label: 'Đang xử lý', color: 'bg-orange-100 text-orange-800' },
  resolved: { label: 'Đã giải quyết', color: 'bg-green-100 text-green-800' },
  closed: { label: 'Đã đóng', color: 'bg-gray-100 text-gray-800' }
}

const priorityConfig: Record<string, { label: string; color: string }> = {
  low: { label: 'Thấp', color: 'text-gray-600' },
  medium: { label: 'TB', color: 'text-blue-600' },
  high: { label: 'Cao', color: 'text-orange-600' },
  urgent: { label: 'Khẩn', color: 'text-red-600 font-bold' }
}

export default function EmployeeSupportPage() {
  const router = useRouter()
  const { user, employee } = useAuthStore()
  const [tickets, setTickets] = useState<Ticket[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState('all')
  const [priorityFilter, setPriorityFilter] = useState('all')
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null)

  // Check permission
  useEffect(() => {
    const position = employee?.position || user?.position
    if (user?.role !== 'ADMIN' && position !== 'CSKH') {
      toast.error('Bạn không có quyền truy cập trang này')
      router.push('/employee')
      return
    }
    loadTickets()
  }, [user, employee])

  const loadTickets = async () => {
    setLoading(true)
    try {
      const res = await supportApi.listTickets()
      if (res.success) {
        setTickets(res.data || [])
      }
    } catch (error) {
      console.error('Error loading tickets:', error)
      toast.error('Không thể tải danh sách tickets')
    } finally {
      setLoading(false)
    }
  }

  const filteredTickets = tickets.filter(ticket => {
    const matchesSearch = 
      ticket.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      ticket.customerName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      ticket.customerEmail?.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesStatus = statusFilter === 'all' || ticket.status === statusFilter
    const matchesPriority = priorityFilter === 'all' || ticket.priority === priorityFilter
    return matchesSearch && matchesStatus && matchesPriority
  })

  const stats = {
    total: tickets.length,
    open: tickets.filter(t => t.status === 'open').length,
    pending: tickets.filter(t => t.status === 'pending' || t.status === 'in_progress').length,
    resolved: tickets.filter(t => t.status === 'resolved' || t.status === 'closed').length,
    urgent: tickets.filter(t => t.priority === 'urgent' && t.status !== 'closed').length
  }

  const formatDate = (dateString: string) => {
    if (!dateString) return ''
    return new Date(dateString).toLocaleDateString('vi-VN', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    })
  }

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Quản lý yêu cầu hỗ trợ</h1>
          <p className="text-gray-500">Xử lý và phản hồi tickets từ khách hàng</p>
        </div>
        <button onClick={loadTickets} className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
          <FiRefreshCw className={loading ? 'animate-spin' : ''} />
          Làm mới
        </button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow-sm p-4">
          <div className="flex items-center gap-3">
            <div className="bg-gray-100 p-2 rounded-lg"><FiMessageCircle className="w-5 h-5 text-gray-600" /></div>
            <div>
              <p className="text-2xl font-bold">{stats.total}</p>
              <p className="text-sm text-gray-500">Tổng tickets</p>
            </div>
          </div>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <div className="flex items-center gap-3">
            <div className="bg-blue-100 p-2 rounded-lg"><FiMessageCircle className="w-5 h-5 text-blue-600" /></div>
            <div>
              <p className="text-2xl font-bold">{stats.open}</p>
              <p className="text-sm text-gray-500">Mới</p>
            </div>
          </div>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <div className="flex items-center gap-3">
            <div className="bg-orange-100 p-2 rounded-lg"><FiClock className="w-5 h-5 text-orange-600" /></div>
            <div>
              <p className="text-2xl font-bold">{stats.pending}</p>
              <p className="text-sm text-gray-500">Đang xử lý</p>
            </div>
          </div>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <div className="flex items-center gap-3">
            <div className="bg-green-100 p-2 rounded-lg"><FiCheckCircle className="w-5 h-5 text-green-600" /></div>
            <div>
              <p className="text-2xl font-bold">{stats.resolved}</p>
              <p className="text-sm text-gray-500">Đã xử lý</p>
            </div>
          </div>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <div className="flex items-center gap-3">
            <div className="bg-red-100 p-2 rounded-lg"><FiAlertCircle className="w-5 h-5 text-red-600" /></div>
            <div>
              <p className="text-2xl font-bold">{stats.urgent}</p>
              <p className="text-sm text-gray-500">Khẩn cấp</p>
            </div>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative">
            <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Tìm theo tiêu đề, tên KH, email..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div className="flex items-center gap-2">
            <FiFilter className="text-gray-400" />
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="border border-gray-200 rounded-lg px-3 py-2">
              <option value="all">Tất cả trạng thái</option>
              <option value="open">Đang mở</option>
              <option value="pending">Chờ xử lý</option>
              <option value="in_progress">Đang xử lý</option>
              <option value="resolved">Đã giải quyết</option>
              <option value="closed">Đã đóng</option>
            </select>
            <select value={priorityFilter} onChange={(e) => setPriorityFilter(e.target.value)} className="border border-gray-200 rounded-lg px-3 py-2">
              <option value="all">Tất cả ưu tiên</option>
              <option value="urgent">Khẩn cấp</option>
              <option value="high">Cao</option>
              <option value="medium">Trung bình</option>
              <option value="low">Thấp</option>
            </select>
          </div>
        </div>
      </div>

      {/* Tickets Table */}
      <div className="bg-white rounded-lg shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-8 text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
            <p className="text-gray-500 mt-4">Đang tải...</p>
          </div>
        ) : filteredTickets.length === 0 ? (
          <div className="p-8 text-center">
            <FiMessageCircle className="w-12 h-12 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-500">Không có tickets nào</p>
          </div>
        ) : (
          <table className="w-full">
            <thead className="bg-gray-50 border-b">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tiêu đề</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Khách hàng</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ưu tiên</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ngày tạo</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filteredTickets.map((ticket) => {
                const status = statusConfig[ticket.status] || statusConfig.open
                const priority = priorityConfig[ticket.priority] || priorityConfig.medium
                return (
                  <tr key={ticket.id} className="hover:bg-gray-50 cursor-pointer" onClick={() => setSelectedTicket(ticket)}>
                    <td className="px-4 py-3 text-sm text-gray-500">#{ticket.id}</td>
                    <td className="px-4 py-3">
                      <p className="font-medium text-gray-900 truncate max-w-xs">{ticket.title}</p>
                      {ticket.category && <span className="text-xs text-gray-500">{ticket.category}</span>}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-2">
                        <FiUser className="w-4 h-4 text-gray-400" />
                        <div>
                          <p className="text-sm text-gray-900">{ticket.customerName || 'N/A'}</p>
                          <p className="text-xs text-gray-500">{ticket.customerEmail}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${status.color}`}>{status.label}</span>
                    </td>
                    <td className="px-4 py-3">
                      <span className={`text-sm ${priority.color}`}>{priority.label}</span>
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-500">{formatDate(ticket.createdAt)}</td>
                    <td className="px-4 py-3"><FiChevronRight className="w-5 h-5 text-gray-400" /></td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* Ticket Detail Modal */}
      {selectedTicket && (
        <TicketDetailModal
          ticket={selectedTicket}
          onClose={() => setSelectedTicket(null)}
          onUpdate={loadTickets}
        />
      )}
    </div>
  )
}

// Ticket Detail Modal Component
function TicketDetailModal({ ticket, onClose, onUpdate }: { ticket: Ticket; onClose: () => void; onUpdate: () => void }) {
  const [ticketDetail, setTicketDetail] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [replyContent, setReplyContent] = useState('')
  const [sending, setSending] = useState(false)

  useEffect(() => {
    loadTicketDetail()
  }, [ticket.id])

  const loadTicketDetail = async () => {
    setLoading(true)
    try {
      const res = await supportApi.getById(ticket.id)
      if (res.success) {
        setTicketDetail(res.data)
      }
    } catch (error) {
      console.error('Error loading ticket detail:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleReply = async () => {
    if (!replyContent.trim()) return
    setSending(true)
    try {
      const res = await supportApi.replyTicket(ticket.id, { content: replyContent })
      if (res.success) {
        toast.success('Đã gửi phản hồi')
        setReplyContent('')
        loadTicketDetail()
      }
    } catch (error: any) {
      toast.error(error.message || 'Không thể gửi phản hồi')
    } finally {
      setSending(false)
    }
  }

  const handleClose = async () => {
    if (!confirm('Bạn có chắc muốn đóng ticket này?')) return
    try {
      const res = await supportApi.closeTicket(ticket.id)
      if (res.success) {
        toast.success('Đã đóng ticket')
        onUpdate()
        onClose()
      }
    } catch (error: any) {
      toast.error(error.message || 'Không thể đóng ticket')
    }
  }

  const status = statusConfig[ticket.status] || statusConfig.open
  const isOpen = ticket.status !== 'closed' && ticket.status !== 'resolved'

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="p-4 border-b flex items-center justify-between">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-sm text-gray-500">#{ticket.id}</span>
              <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${status.color}`}>{status.label}</span>
            </div>
            <h2 className="text-lg font-semibold text-gray-900 mt-1">{ticket.title}</h2>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-lg"><FiX className="w-5 h-5" /></button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-4">
          {loading ? (
            <div className="text-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
            </div>
          ) : (
            <>
              {/* Customer Info */}
              <div className="bg-gray-50 rounded-lg p-3 mb-4">
                <p className="text-sm"><span className="text-gray-500">Khách hàng:</span> {ticketDetail?.customerName || ticket.customerName}</p>
                <p className="text-sm"><span className="text-gray-500">Email:</span> {ticketDetail?.customerEmail || ticket.customerEmail}</p>
              </div>

              {/* Original Content */}
              <div className="mb-4">
                <p className="text-sm text-gray-500 mb-1">Nội dung:</p>
                <p className="text-gray-700 whitespace-pre-wrap">{ticketDetail?.content}</p>
              </div>

              {/* Messages */}
              <div className="space-y-3">
                <p className="text-sm text-gray-500">Trao đổi ({ticketDetail?.messages?.length || 0}):</p>
                {ticketDetail?.messages?.map((msg: any, idx: number) => (
                  <div key={idx} className={`p-3 rounded-lg ${msg.sender === 'support' ? 'bg-blue-50 ml-4' : 'bg-gray-100 mr-4'}`}>
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs font-medium">{msg.sender === 'support' ? 'Hỗ trợ viên' : 'Khách hàng'}</span>
                      <span className="text-xs text-gray-400">{new Date(msg.createdAt).toLocaleString('vi-VN')}</span>
                    </div>
                    <p className="text-sm">{msg.content}</p>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>

        {/* Footer */}
        {isOpen && (
          <div className="p-4 border-t">
            <textarea
              value={replyContent}
              onChange={(e) => setReplyContent(e.target.value)}
              placeholder="Nhập nội dung phản hồi..."
              rows={3}
              className="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none mb-3"
            />
            <div className="flex justify-between">
              <button onClick={handleClose} className="px-4 py-2 border border-red-200 text-red-600 rounded-lg hover:bg-red-50">
                Đóng ticket
              </button>
              <button onClick={handleReply} disabled={sending || !replyContent.trim()} className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
                {sending ? 'Đang gửi...' : 'Gửi phản hồi'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
