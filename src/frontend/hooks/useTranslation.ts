'use client'

import { useLanguageStore } from '@/store/languageStore'
import { translations, TranslationKey } from '@/translations'

export const useTranslation = () => {
  const { currentLanguage } = useLanguageStore()
  
  const t = (key: TranslationKey): string => {
    return translations[currentLanguage][key] || translations.vi[key] || key
  }
  
  return { t, currentLanguage }
}
