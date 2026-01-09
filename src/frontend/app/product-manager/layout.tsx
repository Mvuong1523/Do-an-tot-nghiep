import HorizontalNav from '@/components/layout/HorizontalNav'
import HydratedLayout from '@/components/HydratedLayout'

export default function ProductManagerLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <HydratedLayout>
      <div className="min-h-screen bg-gray-50">
        <HorizontalNav role="PRODUCT_MANAGER" />
        <main>
          {children}
        </main>
      </div>
    </HydratedLayout>
  )
}
