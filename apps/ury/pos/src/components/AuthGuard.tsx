import React, { useEffect, useState } from 'react';
import { useRootStore } from '../store/root-store';
import { Button } from './ui/button';
import { Spinner } from './ui/spinner';
import { RefreshCw } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface Props {
  children: React.ReactNode;
}

const AuthGuard: React.FC<Props> = ({ children }) => {
  const { t } = useTranslation();
  const {
    checkAuth,
    user,
    isLoading: authLoading,
    error: authError,
    fetchPosProfile,
    posProfile,
    isLoading: configLoading,
    error: configError,
    hasAccess,
  } = useRootStore();

  const [isRechecking, setIsRechecking] = useState(false);

  useEffect(() => {
    // Start auth check
    checkAuth();
  }, [checkAuth]);

  // Once we have a user, fetch POS profile
  useEffect(() => {
    if (user) {
      fetchPosProfile();
    }
  }, [user, fetchPosProfile]);

  // Show loading state while either auth or config is loading
  if (authLoading || (user && configLoading) || isRechecking) {
    return (
      <div className="min-h-screen">
        <Spinner />
      </div>
    );
  }

  if (authError || configError) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="text-red-600 text-xl mb-4">⚠️</div>
          <h2 className="text-xl font-semibold text-gray-800 mb-2">{t('access_denied')}</h2>
          <p className="text-gray-600">{authError || configError}</p>
        </div>
      </div>
    );
  }

  if (!user) {
    // The checkAuth function will handle the redirect to login
    return null;
  }

  if (!posProfile) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="text-amber-600 text-xl mb-4">⚠️</div>
          <h2 className="text-xl font-semibold text-gray-800 mb-2">{t('config_error')}</h2>
          <p className="text-gray-600">{t('pos_profile_not_configured')}</p>
        </div>
      </div>
    );
  }

  if (!hasAccess) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="text-amber-600 text-xl mb-4">🔒</div>
          <h2 className="text-xl font-semibold text-gray-800 mb-2">{t('permission_required')}</h2>
          <p className="text-gray-600">{t('no_permission')}</p>
          <p className="text-sm text-gray-500 mt-2">{t('required_roles')} {posProfile.role_allowed_for_billing.map(r => r.role).join(', ')}</p>
          <Button 
            variant="outline"
            className="mt-4"
            onClick={async () => {
              setIsRechecking(true);
              try {
                await fetchPosProfile(true); // Force refresh the POS profile
              } finally {
                setIsRechecking(false);
              }
            }}
          >
            <RefreshCw className="w-4 h-4 mr-2" />
            {t('recheck_permissions')}
          </Button>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};

export default AuthGuard; 