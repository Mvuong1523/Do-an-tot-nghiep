import HorizontalNav from '@/components/layout/HorizontalNav'

export default function SalesLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="min-h-screen bg-gray-50">
      <HorizontalNav role="SALES" />
      <main>
        {children}
      </main>
    </div>
  )
}
