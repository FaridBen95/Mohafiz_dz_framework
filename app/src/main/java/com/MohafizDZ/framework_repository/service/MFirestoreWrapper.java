package com.MohafizDZ.framework_repository.service;

import android.content.Context;
import android.util.Log;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MFirestoreWrapper {
    public static final String TAG = MFirestoreWrapper.class.getSimpleName();
    public static final String RUN_QUERY_URL = "https://firestore.googleapis.com/v1/projects/"+ MConstants.PROJECT_ID +
            "/databases/(default)/documents/:runQuery";
    public static final String COMMIT_UPDATES_URL = "https://firestore.googleapis.com/v1/projects/"+ MConstants.PROJECT_ID +
            "/databases/(default)/documents:commit";
    public static final String UPDATE_PATCH_URL = "https://firestore.googleapis.com/v1/projects/"+MConstants.PROJECT_ID+"/databases/(default)/documents/";
    private RequestQueue requestQueue = null;
    private int TIME_OUT = 60000;

    private Context mContext;

    public MFirestoreWrapper(Context context){
        this.mContext = context;
        if(requestQueue == null) {
//            requestQueue = Volley.newRequestQueue(mContext, new HttpClientStack(new DefaultHttpClient()));
            requestQueue = Volley.newRequestQueue(mContext, new HurlStack(null, ClientSSLSocketFactory.createSslSocketFactory()));
        }
    }

    public String generateReadQuery(String modelName, FilterObject filterObject) {
        return generateReadQuery(modelName, filterObject, 0, null, null, null);
    }

    public String generateReadQuery(String modelName, FilterObject filterObject, int limit, List<String> likeFields, String likeObject, OrderBy orderBy) {
        String startQuery = "{\n" +
                "  \"structuredQuery\": {\n" +
                "    \"from\": [\n" +
                "      {\n" +
                "        \"collectionId\": \""+ modelName +"\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"where\": {" +
                filterObject.generateCompositeFilter()+"\n"+
                "}\n";
        StringBuilder query = new StringBuilder(startQuery);
        if(limit != 0){
            query.append("    ,\"limit\":");
            query.append(limit);
            query.append("\n");
        }
        if(orderBy != null){
            query.append("    ,\"orderBy\": [");
            query.append(orderBy.generateOrderBy());
            query.append("    ]");
        }else {
            if (likeFields != null && likeFields.size() != 0 && likeObject != null) {
                query.append("    ,\"orderBy\": [");
                for (String fieldPath : likeFields) {
                    query.append("      {");
                    query.append("        \"field\": {");
                    query.append("          \"fieldPath\": \"");
                    query.append(fieldPath).append("\"");
                    query.append("        }");
                    query.append("      },");
                }
                query.deleteCharAt(query.lastIndexOf(","));
                query.append("    ],");
                query.append("    \"startAt\": {");
                query.append("      \"values\": [");
                query.append("        {");
                query.append("          \"stringValue\": \"");
                query.append(likeObject).append("\"");
                query.append("        }");
                query.append("      ]");
                query.append("    }");
//            query.append("    },");
//            query.append("    \"endAt\": {");
//            query.append("      \"values\": [");
//            query.append("        {");
//            query.append("          \"stringValue\": \"");
//            query.append(likeObject) .append("\"");
//            query.append("        }");
//            query.append("      ]");
//            query.append("    }");
            }
        }
        String endQuery ="  }\n" +
                "}";
        query.append(endQuery);
        return query.toString();
    }

    public String generateReadQuery(String modelName, FilterObject filterObject, int limit, int offset, List<String> likeFields, String likeObject, OrderBy orderBy) {
        String startQuery = "{\n" +
                "  \"structuredQuery\": {\n" +
                "    \"from\": [\n" +
                "      {\n" +
                "        \"collectionId\": \""+ modelName +"\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"where\": {" +
                filterObject.generateCompositeFilter()+"\n"+
                "}\n";
        StringBuilder query = new StringBuilder(startQuery);
        if(limit != 0){
            query.append("    ,\"limit\":");
            query.append(limit);
            query.append("\n");
        }
        if(offset != 0){
            query.append("    ,\"offset\":");
            query.append(offset);
            query.append("\n");
        }
        if(orderBy != null){
            query.append("    ,\"orderBy\": [");
            query.append(orderBy.generateOrderBy());
            query.append("    ]");
        }else {
            if (likeFields != null && likeFields.size() != 0 && likeObject != null) {
                query.append("    ,\"orderBy\": [");
                for (String fieldPath : likeFields) {
                    query.append("      {");
                    query.append("        \"field\": {");
                    query.append("          \"fieldPath\": \"");
                    query.append(fieldPath).append("\"");
                    query.append("        }");
                    query.append("      },");
                }
                query.deleteCharAt(query.lastIndexOf(","));
                query.append("    ],");
                query.append("    \"startAt\": {");
                query.append("      \"values\": [");
                query.append("        {");
                query.append("          \"stringValue\": \"");
                query.append(likeObject).append("\"");
                query.append("        }");
                query.append("      ]");
                query.append("    }");
//            query.append("    },");
//            query.append("    \"endAt\": {");
//            query.append("      \"values\": [");
//            query.append("        {");
//            query.append("          \"stringValue\": \"");
//            query.append(likeObject) .append("\"");
//            query.append("        }");
//            query.append("      ]");
//            query.append("    }");
            }
        }
        String endQuery ="  }\n" +
                "}";
        query.append(endQuery);
        return query.toString();
    }

    public String generatePatchQuery(Map<String, Object> record) {
        String startQuery = "{\n" +
                "  \"fields\": {\n" ;
        String endQuery = "  }\n" +
                "}";
        StringBuilder queryBuilder = new StringBuilder(startQuery);
        for(String key : record.keySet()){
            Object val = record.get(key);
            queryBuilder.append("    \"").append(key).append("\": {");
            queryBuilder.append("      \"").append(getFieldType(val)).append(jsonValueOf(val));
//            queryBuilder.append("      \"").append(getFieldType(val)).append("\": \"").append(val).append("\"");
            queryBuilder.append("    },");
        }
        queryBuilder.deleteCharAt(queryBuilder.lastIndexOf(","));
        queryBuilder.append(endQuery);
        return queryBuilder.toString();
    }

    public Object jsonValueOf(Object val){
        StringBuilder objectBuilder = new StringBuilder();
        if(val instanceof List){
            objectBuilder.append("\":{\n");
            objectBuilder.append("              \"").append("values\"").append(": [\n");
            for(Object key : (List) val){
                objectBuilder.append("                {");
                objectBuilder.append("                  \"").append("stringValue\": \"");
                objectBuilder.append(key);
                objectBuilder.append("\"\n");
                objectBuilder.append("                },");
            }
            try {
                objectBuilder.deleteCharAt(objectBuilder.lastIndexOf(","));
            }catch (Exception ignored){}
            objectBuilder.append("              ]");
            objectBuilder.append("}\n");
        }else{
            objectBuilder.append("\": \"").append(val).append("\"");
        }
        return objectBuilder.toString();
    }

    public String generateBatchWriteDeleteQuery(String modelName, List<String> records) {
        String startQuery = "{\n" +
                "  \"writes\": [\n";
        String endQuery = "  ]\n" +
                "}";
        StringBuilder queryBuilder = new StringBuilder(startQuery);
        for(String id : records){
            String path = "projects/" + MConstants.PROJECT_ID+"/databases/(default)/documents/"+ modelName+"/"+id;
            queryBuilder.append("    {");
            queryBuilder.append("      \"").append("delete").append("\": \"").append(path).append("\"");
            queryBuilder.append("    },");
        }
        queryBuilder.deleteCharAt(queryBuilder.lastIndexOf(","));
        queryBuilder.append(endQuery);
        return queryBuilder.toString();
    }

    public String generateBatchWriteUpdateQuery(String modelName, Map<String, Map<String, Object>> records) {
        String startQuery = "{\n" +
                "  \"writes\": [\n";
        String endQuery = "  ]\n" +
                "}";
        StringBuilder queryBuilder = new StringBuilder(startQuery);
        for(Map<String, Object> record : records.values()){
            String path = "projects/" + MConstants.PROJECT_ID+"/databases/(default)/documents/"+ modelName+"/"+record.get(Col.SERVER_ID);
            queryBuilder.append("    {\n");
            queryBuilder.append("      \"update\": {\n");
            queryBuilder.append("        \"name\": \"").append(path).append("\",\n");
            queryBuilder.append("        \"fields\": {");
            for(String key : record.keySet()){
                Object val = record.get(key);
                queryBuilder.append("          \"").append(key).append("\": {");
//                queryBuilder.append("            \"").append(getFieldType(val)).append("\": \"").append(val).append("\"");
                queryBuilder.append("            \"").append(getFieldType(val)).append(jsonValueOf(val));
                queryBuilder.append("          },");
            }
            queryBuilder.deleteCharAt(queryBuilder.lastIndexOf(","));
            queryBuilder.append("        }\n");
            queryBuilder.append("      },\n");
            String startUpdateMask = "      \"updateMask\": {\n" +
                    "        \"fieldPaths\": [\n";
            String endUpdateMask = "        ]\n" +
                    "      }\n" +
                    "    },";
            StringBuilder updateMaskBuilder = new StringBuilder(startUpdateMask);
            for(String key : record.keySet()){
                updateMaskBuilder.append("          \"").append(key).append("\",\n");
            }
            updateMaskBuilder.deleteCharAt(updateMaskBuilder.lastIndexOf(","));
            updateMaskBuilder.append(endUpdateMask);
            queryBuilder.append(updateMaskBuilder);
        }
        queryBuilder.deleteCharAt(queryBuilder.lastIndexOf(","));
        queryBuilder.append(endQuery);
        return queryBuilder.toString();
    }

    public String generatePatchURL(String modelName, String serverId, String... fields){
        StringBuilder url = new StringBuilder(UPDATE_PATCH_URL);
        url.append(modelName).append("/").append(serverId).append("?");
        for(String field : fields){
            url.append("updateMask.fieldPaths=").append(field).append("&");
        }
        url.deleteCharAt(url.lastIndexOf("&"));
        return url.toString();
    }

    public void newJSONPOSTRequest(String url, String postData, final String token, MFirestoreResponse callback) throws JSONException{
        Log.d(TAG,"POST_METHOD");

//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://firestore.googleapis.com")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        RetrofitApiInterface api = retrofit.create(RetrofitApiInterface.class);
//        Call<JSONArray> call = api.getCountries(new JSONObject(postData));
//        try {
//            retrofit2.Response<JSONArray> execute = call.execute();
//            execute.message();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        CustomJsonArrayRequest request = new CustomJsonArrayRequest(Request.Method.POST, url,
                new JSONObject(postData), future, future){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                if(token != null){
                    params.put("Authorization", "Bearer "+ token);
                }
                params.put("Content-Type", "application/json;charset=UTF-8");
                params.put("Accept", "application/json");
                return params;
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };
//        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(2500, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);


        try {
            JSONArray response = future.get(); // this will block
            JSONObject firsrObject = response.getJSONObject(0);
            if(callback != null){
                if(firsrObject == null || firsrObject.toString().equals("") || firsrObject.has("error")) {
                    callback.onError(firsrObject);
                }else{
                    callback.onResponse(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            JSONObject errorObject = new JSONObject();
            errorObject.put("error", message);
            callback.onError(errorObject);
        }
    }

    public void newJSONPATCHRequest(String url, String postData, final String token, MFirestoreResponse callback) throws JSONException{
        Log.d(TAG,"PATCH_METHOD");
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH, url,
                new JSONObject(postData), future, future){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                if(token != null){
                    params.put("Authorization", "Bearer "+ token);
                }
                params.put("Content-Type", "application/json;charset=UTF-8");
                params.put("Accept", "application/json");
                return params;
            }
        };
//        request.setRetryPolicy(new DefaultRetryPolicy(TIME_OUT,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setShouldCache(false);
        requestQueue.add(request);

        try {
            JSONObject response = future.get(); // this will block
            if(callback != null){
                if(response == null || response.has("error")) {
                    callback.onError(response);
                }else{
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(response);
                    callback.onResponse(jsonArray);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            String message = e.getMessage();
            JSONObject errorObject = new JSONObject();
            errorObject.put("error", message);
            callback.onError(errorObject);
        }
    }

    public void newJSONBatchWriteRequest(String url, String postData, final String token, MFirestoreResponse callback) throws JSONException{
        Log.d(TAG,"POST");
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,
                new JSONObject(postData), future, future){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                if(token != null){
                    params.put("Authorization", "Bearer "+ token);
                }
                params.put("Content-Type", "application/json;charset=UTF-8");
                params.put("Accept", "application/json");
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);

        try {
            JSONObject response = future.get(); // this will block
            if(callback != null){
                if(response == null || response.toString().equals("") || response.has("error")) {
                    callback.onError(response);
                }else{
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(response);
                    callback.onResponse(jsonArray);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            String message = e.getMessage();
            JSONObject errorObject = new JSONObject();
            errorObject.put("error", message);
            callback.onError(errorObject);
        }
    }

    public String getFieldType(Object val){
        return val instanceof String? "stringValue":
                (val instanceof Boolean ? "booleanValue" :
                        ((val instanceof Integer || val instanceof Long)? "integerValue" :
                                (val instanceof Float || val instanceof Double)? "doubleValue":
                                        (val instanceof List)? "arrayValue" : "stringValue"));
    }

}
