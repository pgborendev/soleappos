import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { setLanguage, getLanguage } from '../i18n';

const LanguageSwitcher = () => {
  const { i18n } = useTranslation();
  const [current, setCurrent] = useState<'en' | 'km'>(getLanguage());

  const toggle = () => {
    const next = current === 'en' ? 'km' : 'en';
    setLanguage(next);
    setCurrent(next);
  };

  return (
    <button
      onClick={toggle}
      className="flex items-center gap-1.5 px-3 py-1.5 rounded-full border border-gray-200 bg-gray-50 hover:bg-gray-100 text-sm font-medium text-gray-700 transition-colors select-none"
      title={i18n.language === 'en' ? 'Switch to Khmer' : 'ប្ដូរទៅភាសាអង់គ្លេស'}
    >
      <span className="text-base leading-none">{current === 'en' ? '🇰🇭' : '🇺🇸'}</span>
      <span>{current === 'en' ? 'ភាសាខ្មែរ' : 'English'}</span>
    </button>
  );
};

export default LanguageSwitcher;
