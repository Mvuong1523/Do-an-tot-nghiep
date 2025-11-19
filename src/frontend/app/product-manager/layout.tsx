import HorizontalNav from '@/components/layout/HorizontalNav'

export default function ProductManagerLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="min-h-screen bg-gray-50">
      <HorizontalNav role="PRODUCT_MANAGER" />
      <main>
        {children}
      </main>
    </div>
  )
}
