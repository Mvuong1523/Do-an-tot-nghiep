import HorizontalNav from '@/components/layout/HorizontalNav'

export default function WarehouseLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="min-h-screen bg-gray-50">
      <HorizontalNav role="WAREHOUSE" />
      <main>
        {children}
      </main>
    </div>
  )
}
