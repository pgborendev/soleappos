import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import en from './locales/en';
import km from './locales/km';

const LANG_KEY = 'ury_language';

const initialLang = (localStorage.getItem(LANG_KEY) as 'en' | 'km') || 'en';
document.documentElement.lang = initialLang;

i18n
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      km: { translation: km },
    },
    lng: initialLang,
    fallbackLng: 'en',
    interpolation: { escapeValue: false },
  });

export function setLanguage(lang: 'en' | 'km') {
  localStorage.setItem(LANG_KEY, lang);
  document.documentElement.lang = lang;
  i18n.changeLanguage(lang);
}

export function getLanguage(): 'en' | 'km' {
  return (localStorage.getItem(LANG_KEY) as 'en' | 'km') || 'en';
}

export default i18n;
