import { useState } from 'react'

function App() {
  const [count, setCount] = useState(0)

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-100">
        <div className="container-custom py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-display font-bold text-gradient">
              Tech World
            </h1>
            <nav className="flex gap-4">
              <button className="btn btn-outline">Đăng nhập</button>
              <button className="btn btn-primary">Đăng ký</button>
            </nav>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="container-custom py-16">
        <div className="text-center space-y-6 animate-fade-in">
          <h1 className="text-5xl md:text-6xl font-display font-bold">
            Welcome to{' '}
            <span className="text-gradient">React 19</span>
          </h1>

          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Production-ready e-commerce platform built with React 19, TypeScript, and TailwindCSS
          </p>

          {/* Interactive Card */}
          <div className="max-w-md mx-auto mt-12">
            <div className="card-hover p-8 space-y-6">
              <div className="flex items-center justify-center">
                <div className="w-20 h-20 bg-gradient-primary rounded-2xl flex items-center justify-center text-white text-3xl font-bold shadow-lg">
                  {count}
                </div>
              </div>

              <button
                onClick={() => setCount(count + 1)}
                className="btn btn-primary w-full text-lg py-3"
              >
                Click me! 🚀
              </button>

              <div className="flex gap-3">
                <button
                  onClick={() => setCount(count - 1)}
                  className="btn btn-outline flex-1"
                >
                  Decrease
                </button>
                <button
                  onClick={() => setCount(0)}
                  className="btn btn-secondary flex-1"
                >
                  Reset
                </button>
              </div>

              <div className="glass p-4 rounded-lg">
                <p className="text-sm text-gray-600">
                  ✨ TailwindCSS is working perfectly!
                </p>
                <p className="text-xs text-gray-500 mt-2">
                  Try clicking the buttons to see the animations
                </p>
              </div>
            </div>
          </div>

          {/* Feature Cards */}
          <div className="grid md:grid-cols-3 gap-6 mt-16 animate-slide-up">
            <div className="card p-6 text-left">
              <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center mb-4">
                <span className="text-2xl">⚡</span>
              </div>
              <h3 className="text-lg font-semibold mb-2">React 19</h3>
              <p className="text-gray-600 text-sm">
                Latest React with improved performance and new features
              </p>
            </div>

            <div className="card p-6 text-left">
              <div className="w-12 h-12 bg-secondary-100 rounded-lg flex items-center justify-center mb-4">
                <span className="text-2xl">🎨</span>
              </div>
              <h3 className="text-lg font-semibold mb-2">TailwindCSS</h3>
              <p className="text-gray-600 text-sm">
                Utility-first CSS framework for rapid UI development
              </p>
            </div>

            <div className="card p-6 text-left">
              <div className="w-12 h-12 bg-accent-100 rounded-lg flex items-center justify-center mb-4">
                <span className="text-2xl">🔥</span>
              </div>
              <h3 className="text-lg font-semibold mb-2">TypeScript</h3>
              <p className="text-gray-600 text-sm">
                Type-safe development with excellent IDE support
              </p>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-gray-900 text-white py-8 mt-16">
        <div className="container-custom text-center">
          <p className="text-gray-400">
            Built with ❤️ using React 19 + TypeScript + TailwindCSS
          </p>
        </div>
      </footer>
    </div>
  )
}

export default App
