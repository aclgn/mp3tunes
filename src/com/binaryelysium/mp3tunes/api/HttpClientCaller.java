package com.binaryelysium.mp3tunes.api;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.binaryelysium.mp3tunes.api.Session.LoginException;

import android.util.Log;

public class HttpClientCaller
{
    @SuppressWarnings("serial")
    public class CallerException extends RuntimeException {

        public CallerException() {
        }

        public CallerException(Throwable cause) {
            super(cause);
        }

        public CallerException(String message) {
            super(message);
        }

        public CallerException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final HttpClientCaller instance = new HttpClientCaller();

    private HttpClientCaller() {
    }

    public static HttpClientCaller getInstance() {
        return instance;
    }

    public String call(RemoteMethod method) throws IOException, InvalidSessionException, LockerException, LoginException 
    {
        try {
            HttpClient client = new DefaultHttpClient();
            String url = method.getCall();
            Log.w("Mp3tunes", "Calling: " + url);
            HttpGet get = new HttpGet(url);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response = client.execute(get, responseHandler);
            client.getConnectionManager().shutdown();
            return response;
        } catch (HttpResponseException e) {
            Log.e("Mp3Tunes", "Status code: " + Integer.toString(e.getStatusCode()));
            if (e.getStatusCode() == 401) {
                if (!method.isLogin()) {
                    if (handleBadSession()) {
                        method.updateSession();
                        return call(method);
                    }
                }
            }
            throw e;
        } catch (UnknownHostException e) {
            Log.e("Mp3Tunes", "UnknownHostException: what do we do?");
            throw e;
        } catch (SocketException e) {
            Log.e("Mp3Tunes", "SocketException: what do we do?");
            throw e;
        } catch (IOException e) {
            Log.e("Mp3Tunes", Log.getStackTraceString(e));
            throw e;
        }
    }
    
    public byte[] callBytes(RemoteMethod method) throws IOException, InvalidSessionException, LockerException, LoginException 
    {
        try {
            HttpClient client = new DefaultHttpClient();
            String url = method.getCall();
            HttpGet get = new HttpGet(url);
            ResponseHandler<byte[]> responseHandler = new BytesResponseHandler();
            byte[] response = client.execute(get, responseHandler);
            client.getConnectionManager().shutdown();
            return response;
        } catch (HttpResponseException e) {
            Log.e("Mp3Tunes", "Status code: " + Integer.toString(e.getStatusCode()));
            if (e.getStatusCode() == 401) {
                if (!method.isLogin()) {
                    if (handleBadSession()) {
                        method.updateSession();
                        return callBytes(method);
                    }
                }
            }
            throw e;
        } catch (UnknownHostException e) {
            Log.e("Mp3Tunes", "UnknownHostException: what do we do?");
            throw e;
        } catch (SocketException e) {
            Log.e("Mp3Tunes", "SocketException: what do we do?");
            throw e;
        } catch (IOException e) {
            Log.e("Mp3Tunes", Log.getStackTraceString(e));
            throw e;
        }
    }
    
    
    
    private boolean handleBadSession() throws LockerException, LoginException 
    {
            LockerContext c = LockerContext.instance();
            
            String userName = c.getUserName();
            String password = c.getPassword();
            
            if (password == null || userName == null) 
                return false;
            
            Locker l = new Locker();
            l.refreshSession(userName, password);
            return true;
    }
  
    private class BytesResponseHandler implements ResponseHandler<byte[]>  
    {
        public byte[] handleResponse(HttpResponse response) throws ClientProtocolException, IOException 
        {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toByteArray(entity);
            } else {
                return null;
            }
        }

    };
    
}
