import HorizontalNav from '@/components/layout/HorizontalNav'

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="min-h-screen bg-gray-50">
      <HorizontalNav role="ADMIN" />
      <main>
        {children}
      </main>
    </div>
  )
}
