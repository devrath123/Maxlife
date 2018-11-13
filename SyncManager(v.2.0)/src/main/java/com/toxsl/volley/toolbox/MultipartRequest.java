package com.toxsl.volley.toolbox;

import android.widget.Toast;

import com.toxsl.volley.AuthFailureError;
import com.toxsl.volley.NetworkResponse;
import com.toxsl.volley.Request;
import com.toxsl.volley.Response;
import com.toxsl.volley.VolleyLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.HttpEntity;

/**
 * Created by ankush.walia on 14-8-16.
 */
public class MultipartRequest extends Request<String> {
    private HttpEntity mHttpEntity;
    private Response.Listener mListener;

    public MultipartRequest(String url, RequestParams builder,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener, int methodType) {
        super(methodType, url, errorListener);
        mListener = listener;
        if (builder == null) {
            try {
                mHttpEntity = new RequestParams().getEntity();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mHttpEntity = builder.getEntity();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public MultipartRequest(String url, ResponseHandlerInterface responseHandlerInterface, RequestParams builder,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener, int methodType) {
        super(methodType, url, errorListener);
        mListener = listener;
        if (builder == null) {
            try {
                mHttpEntity = new RequestParams().getEntity(responseHandlerInterface);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mHttpEntity = builder.getEntity(responseHandlerInterface);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBodyContentType() {
        return mHttpEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mHttpEntity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }

        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}