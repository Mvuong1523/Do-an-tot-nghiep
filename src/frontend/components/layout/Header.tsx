'use client'

import { useState } from 'react'
import Link from 'next/link'
import Image from 'next/image'
import { FiSearch, FiShoppingCart, FiUser, FiHeart, FiX, FiChevronDown } from 'react-icons/fi'
import { useCartStore } from '@/store/cartStore'
import { useLanguageStore } from '@/store/languageStore'
import { useTranslation } from '@/hooks/useTranslation'

export default function Header() {
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const [isLanguageOpen, setIsLanguageOpen] = useState(false)
  
  const { getCartCount } = useCartStore()
  const { currentLanguage, setLanguage } = useLanguageStore()
  const { t } = useTranslation()

  const navigationLinks = [
    { name: t('home'), href: '/' },
    { name: t('products'), href: '/products' },
    { name: t('contact'), href: '/contact' },
  ]

  const languages = [
    { code: 'vi', name: 'Tiếng Việt', flag: '🇻🇳' },
    { code: 'en', name: 'English', flag: '🇺🇸' },
  ]

  const handleLanguageChange = (languageCode: 'vi' | 'en') => {
    setLanguage(languageCode)
    setIsLanguageOpen(false)
  }

  return (
    <header className="bg-white shadow-sm sticky top-0 z-50">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between py-4">
          {/* Logo */}
          <Link href="/" className="flex items-center space-x-3">
            <div className="w-12 h-12 bg-navy-600 rounded-lg flex items-center justify-center">
              <img src="/logo.png" alt="Tech World Logo" className="w-10 h-10 object-contain" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-gray-900">TECH WORLD</h1>
            </div>
          </Link>

          {/* Search Bar */}
          <div className="flex-1 max-w-md mx-8">
            <div className="relative">
              <input
                type="text"
                placeholder={t('searchPlaceholder')}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full px-4 py-3 pr-12 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-navy-500 focus:border-transparent"
              />
              <button className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-navy-500">
                <FiSearch size={20} />
              </button>
              {searchQuery && (
                <button 
                  onClick={() => setSearchQuery('')}
                  className="absolute right-10 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  <FiX size={16} />
                </button>
              )}
            </div>
          </div>

          {/* Navigation Links */}
          <div className="hidden md:flex items-center space-x-6">
            {navigationLinks.map((link) => (
              <Link
                key={link.name}
                href={link.href}
                className="text-gray-700 hover:text-navy-500 font-medium transition-colors"
              >
                {link.name}
              </Link>
            ))}
          </div>

          {/* Action Buttons */}
          <div className="flex items-center space-x-3">
            {/* Wishlist Button */}
            <Link href="/wishlist" className="flex items-center space-x-2 bg-white border border-gray-200 rounded-lg px-4 py-2 hover:shadow-md transition-all">
              <FiHeart size={18} className="text-gray-600" />
              <span className="text-gray-700 font-medium">{t('wishlist')}</span>
            </Link>

            {/* Cart Button */}
            <Link href="/cart" className="flex items-center space-x-2 bg-white border border-gray-200 rounded-lg px-4 py-2 hover:shadow-md transition-all relative">
              <FiShoppingCart size={18} className="text-gray-600" />
              <span className="text-gray-700 font-medium">{t('cart')}</span>
              {getCartCount() > 0 && (
                <span className="absolute -top-2 -right-2 bg-navy-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-semibold">
                  {getCartCount()}
                </span>
              )}
            </Link>

            {/* Login Button */}
            <Link href="/login" className="bg-navy-500 text-white px-4 py-2 rounded-lg font-medium hover:bg-navy-600 transition-colors">
              {t('login')}
            </Link>

            {/* Language Dropdown */}
            <div className="relative">
              <button
                onClick={() => setIsLanguageOpen(!isLanguageOpen)}
                className="flex items-center space-x-2 bg-white border border-gray-200 rounded-lg px-4 py-2 hover:shadow-md transition-all"
              >
                <span className="text-gray-700 font-medium">
                  {currentLanguage === 'vi' ? 'Tiếng Việt' : 'English'}
                </span>
                <FiChevronDown size={16} className="text-gray-600" />
              </button>
              
              {isLanguageOpen && (
                <div className="absolute right-0 top-full mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-50">
                  {languages.map((lang) => (
                    <button
                      key={lang.code}
                      className={`w-full flex items-center space-x-3 px-4 py-3 text-left hover:bg-gray-50 transition-colors ${
                        currentLanguage === lang.code ? 'bg-navy-50 text-navy-600' : ''
                      }`}
                      onClick={() => handleLanguageChange(lang.code as 'vi' | 'en')}
                    >
                      <span className="text-lg">{lang.flag}</span>
                      <span className="text-gray-700">{lang.name}</span>
                      {currentLanguage === lang.code && (
                        <span className="ml-auto text-navy-500">✓</span>
                      )}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Mobile Menu Button */}
          <button
            onClick={() => setIsMenuOpen(!isMenuOpen)}
            className="md:hidden p-2 text-gray-700 hover:text-navy-500"
          >
            {isMenuOpen ? <FiX size={24} /> : <FiUser size={24} />}
          </button>
        </div>

        {/* Mobile Menu */}
        {isMenuOpen && (
          <div className="md:hidden border-t py-4">
            <div className="space-y-3">
              {navigationLinks.map((link) => (
                <Link
                  key={link.name}
                  href={link.href}
                  className="block px-4 py-2 text-gray-700 hover:text-navy-500 hover:bg-gray-50 rounded-lg transition-colors"
                  onClick={() => setIsMenuOpen(false)}
                >
                  {link.name}
                </Link>
              ))}
            </div>
          </div>
        )}
      </div>
    </header>
  )
}
