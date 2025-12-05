import HorizontalNav from '@/components/layout/HorizontalNav'
import HydratedLayout from '@/components/HydratedLayout'

export default function SalesLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <HydratedLayout>
      <div className="min-h-screen bg-gray-50">
        <HorizontalNav role="SALES" />
        <main>
          {children}
        </main>
      </div>
    </HydratedLayout>
  )
}
