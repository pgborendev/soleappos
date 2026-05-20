import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { checkPOSOpening, validatePOSClose } from '../lib/pos-opening-api';
import { usePOSStore } from '../store/pos-store';
import POSOpeningDialog from './POSOpeningDialog';

interface POSOpeningProviderProps {
  children: React.ReactNode;
}

type ValidationType = 'opening' | 'closing' | null;

const POSOpeningProvider = ({ children }: POSOpeningProviderProps) => {
  const { t } = useTranslation();
  const [validationType, setValidationType] = useState<ValidationType>(null);
  const [isLoading, setIsLoading] = useState(true);
  const { posProfile } = usePOSStore();

  const checkPOSStatus = async () => {
    try {
      setIsLoading(true);
      const openingResponse = await checkPOSOpening();
      if (openingResponse.message === 1) {
        setValidationType('opening');
        return;
      }
      if (posProfile?.custom_daily_pos_close === 1) {
        try {
          const closeResponse = await validatePOSClose(posProfile.name);
          if (closeResponse.message === 'Failed') {
            setValidationType('closing');
            return;
          }
        } catch (error) {
          console.error('Failed to validate POS close status:', error);
          setValidationType('closing');
          return;
        }
      }
      setValidationType(null);
    } catch (error) {
      console.error('Failed to check POS opening status:', error);
      setValidationType('opening');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (posProfile) {
      checkPOSStatus();
    }
  }, [posProfile]);

  if (isLoading) {
    return (
      <div className="fixed inset-0 bg-white flex items-center justify-center z-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">{t('checking_pos_status')}</p>
        </div>
      </div>
    );
  }

  if (validationType) {
    return <POSOpeningDialog onReload={() => window.location.reload()} type={validationType} />;
  }

  return <>{children}</>;
};

export default POSOpeningProvider;
