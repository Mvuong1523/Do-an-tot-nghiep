'use client'

import { useState, useEffect } from 'react'
import { FiStar, FiUser, FiCalendar } from 'react-icons/fi'
import { reviewApi } from '@/lib/api'

interface Review {
  id: number
  productId: number
  productName: string
  customerId: number
  customerName: string
  orderId: number
  orderCode: string
  rating: number
  comment: string
  createdAt: string
}

interface RatingSummary {
  averageRating: number
  reviewCount: number
  ratingDistribution?: { [key: number]: number }
}

interface ProductReviewsProps {
  productId: number
}

export default function ProductReviews({ productId }: ProductReviewsProps) {
  const [reviews, setReviews] = useState<Review[]>([])
  const [summary, setSummary] = useState<RatingSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [filterRating, setFilterRating] = useState<number | null>(null)

  useEffect(() => {
    loadReviews()
  }, [productId])

  const loadReviews = async () => {
    try {
      const [reviewsRes, summaryRes] = await Promise.all([
        reviewApi.getByProduct(productId),
        reviewApi.getProductSummary(productId)
      ])

      if (reviewsRes.success) {
        setReviews(reviewsRes.data || [])
      }
      if (summaryRes.success) {
        setSummary(summaryRes.data)
      }
    } catch (error) {
      console.error('Error loading reviews:', error)
    } finally {
      setLoading(false)
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  }

  const renderStars = (rating: number, size: number = 16) => {
    return (
      <div className="flex items-center">
        {[1, 2, 3, 4, 5].map((star) => (
          <FiStar
            key={star}
            size={size}
            className={star <= rating ? 'text-yellow-400 fill-current' : 'text-gray-300'}
          />
        ))}
      </div>
    )
  }

  const filteredReviews = filterRating
    ? reviews.filter(r => r.rating === filterRating)
    : reviews

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  return (
    <div>
      <h2 className="text-2xl font-bold mb-6">Đánh giá sản phẩm</h2>

      {/* Rating Summary */}
      {summary && summary.reviewCount > 0 ? (
        <div className="bg-gray-50 rounded-lg p-6 mb-6">
          <div className="flex flex-col md:flex-row md:items-center gap-6">
            {/* Average Rating */}
            <div className="text-center md:border-r md:pr-6">
              <div className="text-5xl font-bold text-yellow-500 mb-2">
                {summary.averageRating.toFixed(1)}
              </div>
              {renderStars(Math.round(summary.averageRating), 24)}
              <p className="text-gray-600 mt-2">{summary.reviewCount} đánh giá</p>
            </div>

            {/* Rating Distribution */}
            <div className="flex-1">
              {[5, 4, 3, 2, 1].map((star) => {
                const count = summary.ratingDistribution?.[star] || 0
                const percentage = summary.reviewCount > 0 
                  ? (count / summary.reviewCount) * 100 
                  : 0
                return (
                  <button
                    key={star}
                    onClick={() => setFilterRating(filterRating === star ? null : star)}
                    className={`flex items-center w-full mb-2 hover:bg-gray-100 rounded p-1 transition-colors ${
                      filterRating === star ? 'bg-yellow-50' : ''
                    }`}
                  >
                    <span className="w-8 text-sm text-gray-600">{star} ★</span>
                    <div className="flex-1 h-3 bg-gray-200 rounded-full mx-2 overflow-hidden">
                      <div
                        className="h-full bg-yellow-400 rounded-full transition-all"
                        style={{ width: `${percentage}%` }}
                      />
                    </div>
                    <span className="w-12 text-sm text-gray-600 text-right">{count}</span>
                  </button>
                )
              })}
            </div>
          </div>

          {filterRating && (
            <div className="mt-4 pt-4 border-t">
              <button
                onClick={() => setFilterRating(null)}
                className="text-blue-600 hover:underline text-sm"
              >
                ✕ Xóa bộ lọc ({filterRating} sao)
              </button>
            </div>
          )}
        </div>
      ) : (
        <div className="text-center py-12 bg-gray-50 rounded-lg">
          <FiStar className="mx-auto text-gray-300 mb-4" size={48} />
          <p className="text-gray-600">Chưa có đánh giá nào</p>
          <p className="text-sm text-gray-500 mt-2">
            Hãy là người đầu tiên đánh giá sản phẩm này
          </p>
        </div>
      )}

      {/* Reviews List */}
      {filteredReviews.length > 0 && (
        <div className="space-y-4">
          {filteredReviews.map((review) => (
            <div key={review.id} className="border rounded-lg p-4 hover:shadow-sm transition-shadow">
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                    <FiUser className="text-blue-600" size={20} />
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">{review.customerName}</p>
                    <div className="flex items-center space-x-2 text-sm text-gray-500">
                      <FiCalendar size={14} />
                      <span>{formatDate(review.createdAt)}</span>
                    </div>
                  </div>
                </div>
                {renderStars(review.rating, 18)}
              </div>

              {review.comment && (
                <p className="text-gray-700 mt-2">{review.comment}</p>
              )}
            </div>
          ))}
        </div>
      )}

      {filterRating && filteredReviews.length === 0 && (
        <div className="text-center py-8 text-gray-500">
          Không có đánh giá {filterRating} sao nào
        </div>
      )}
    </div>
  )
}
