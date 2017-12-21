package com.smarking.mhealthsyria.app.api.auth.provision;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle; import android.text.TextUtils;
import android.util.Log;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.view.login.DeviceProvisionActivity;

import org.w3c.dom.Text;

import java.util.NoSuchElementException;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-28.
 */
public class DeviceProvisioner extends AbstractAccountAuthenticator {
    public static final String TAG = DeviceProvisioner.class.getSimpleName();

    public final static String ARG_KEY_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_KEY_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_KEY_ACCOUNT_NAME = "ACCOUNT_NAME";

    private Context mContext;

    public DeviceProvisioner(Context context){
        super(context);
        mContext = context;
    }

    public Account getAccount() throws IllegalAccessException {
        String accountType = mContext.getResources().getString(R.string.account_type);
        Account[] accounts = AccountManager.get(mContext).getAccountsByType(accountType);

       if(accounts.length == 1){
           Log.e(TAG, "Getting account: " + accounts[0].toString());
            return accounts[0];
        }
        else{
            throw new IllegalAccessException();
        }
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Intent intent = new Intent(mContext, DeviceProvisionActivity.class);
        intent.putExtra(ARG_KEY_ACCOUNT_TYPE, accountType);
        if(authTokenType == null || TextUtils.isEmpty(authTokenType)){
            authTokenType = mContext.getString(R.string.account_tokenType);
        }
        intent.putExtra(ARG_KEY_AUTH_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.e(TAG, "getAuthToken");

        // If the caller requested an authToken type that we don't support, then return an error
        if (!authTokenType.equals(mContext.getString(R.string.account_tokenType))) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        // Extract the username and password from the Account Manager, and ask the server for an appropriate AuthToken.

        String authToken = getAuthToken(account, authTokenType);
        Log.e(TAG, "authToken peek " + authToken);


        if(TextUtils.isEmpty(authToken)){
            Log.e(TAG, "Empty auth token");
            // If the authToken is empty, then we make a server call to get the token
            final Intent intent = new Intent(mContext, DeviceProvisionActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            intent.putExtra(ARG_KEY_ACCOUNT_TYPE, account.type);
            intent.putExtra(ARG_KEY_AUTH_TYPE, authTokenType);
            final Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }
        else{
            // If we get an authToken - we return it
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }
    }

    public String getAuthToken(Account account, String authTokenType){
        if(authTokenType == null || TextUtils.isEmpty(authTokenType)){
            authTokenType = mContext.getString(R.string.account_tokenType);
        }
        if(account == null){
            try {
                account = getAccount();
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Account not found!");
            }
        }
        final AccountManager am = AccountManager.get(mContext);
        return am.peekAuthToken(account, authTokenType);
    }


    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return authTokenType + " (Label)";
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

}
