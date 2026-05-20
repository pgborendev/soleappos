import React from 'react';
import { useTranslation } from 'react-i18next';
import { Spinner } from './ui/spinner';

const InitialLoader: React.FC = () => {
  const { t } = useTranslation();
  return (
    <div className="fixed inset-0 bg-white flex items-center justify-center">
      <div className="text-center">
        <Spinner className="w-12 h-12" />
        <p className="mt-4 text-lg font-medium text-gray-900">{t('loading_ury_pos')}</p>
        <p className="mt-2 text-sm text-gray-500">{t('please_wait_setup')}</p>
      </div>
    </div>
  );
};

export default InitialLoader;
