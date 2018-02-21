
package com.snackpick.wee.membership_visionapi_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.snackpick.wee.membership_visionapi_app.Adapter.AllergyListAdapter;
import com.snackpick.wee.membership_visionapi_app.Models.Allergy;
import com.snackpick.wee.membership_visionapi_app.Models.AllergyIngredient;
import com.snackpick.wee.membership_visionapi_app.Models.Food;
import com.snackpick.wee.membership_visionapi_app.Models.FoodMaterial;
import com.snackpick.wee.membership_visionapi_app.R;
import com.snackpick.wee.membership_visionapi_app.Utils.PackageManagerUtils;
import com.snackpick.wee.membership_visionapi_app.Utils.PermissionUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Vision
    private static final String CLOUD_VISION_API_KEY = "AIzaSyB_CfmE3ikd0HVnxyJQcY2Hb4l0hXkSt7w";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    // Permissions
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mReference;
    private DatabaseReference mUserReference;
    private FirebaseUser currentUser;

    // View
    private Uri intentPhotoUri;
    private AllergyListAdapter mAdapter;
    private ImageButton add_btn;
    private ImageView profilePhoto;
    private ListView mListView;
    private Context mContext = MainActivity.this;
    private ChildEventListener mChildEventListener;
    private TextView userName;

    private ArrayList<String> allergies = new ArrayList<>();
    private GoogleSignInClient mGoogleSignInClient;

    private String barcodeResult;
    private String visionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mReference = mFirebaseDatabase.getReference();
        mAdapter = new AllergyListAdapter(this, 0);

        profilePhoto = findViewById(R.id.profile_photo);
        add_btn = findViewById(R.id.allergy_add_button);
        mListView = findViewById(R.id.my_allergy_listView);
        ImageButton allergy_searchButton = findViewById(R.id.allergy_search_button);
        userName = findViewById(R.id.userName);

        mListView.setAdapter(mAdapter);

        setupFirebaseAuth();
        initFirebaseDatabase();
        initProfile();
        initToolbar();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        allergy_searchButton.setOnClickListener(searchBtnOnClickListener);
        add_btn.setOnClickListener(addBtnOnClickListener);

        ImageButton barcodeSearchBtn = findViewById(R.id.allergy_search_button2);
        barcodeSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                integrator.setPrompt("Scan a barcode");

                integrator.setOrientationLocked(false);
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
            }
        });

        //광고삽입
        AdView mAdView = findViewById(R.id.adView1);
        /*divice Id */
        Log.d("Test_Device_Id", AdRequest.DEVICE_ID_EMULATOR);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

    }

    public void initProfile() {
        if (currentUser.getPhotoUrl() != null) {
            Picasso.with(mContext).load(currentUser.getPhotoUrl().toString()).into(profilePhoto);
            userName.setText(mAuth.getCurrentUser().getDisplayName());
        } else {
            Log.d(TAG, "User Doesn't have Profile.");
        }
    }

    public void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SNACKpick");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public View.OnClickListener searchBtnOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {

            Toast.makeText(MainActivity.this, "업데이트 예정입니다.",
                    Toast.LENGTH_SHORT).show();
            /* AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.dialog_select_prompt)
                    .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startGalleryChooser();
                        }
                    })
                    .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startCamera();
                        }
                    });
            builder.create().show();*/
        }
    };

    public ImageButton.OnClickListener addBtnOnClickListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.allergy_add_button:
                    ConstraintLayout mainLayout = MainActivity.this.findViewById(R.id.layout_main);

                    // inflate the layout of the popup window
                    LayoutInflater inflater = (LayoutInflater)
                            MainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.add_popup, null);

                    final EditText componentEditText= popupView.findViewById(R.id.component_edit_text);

                    // create the popup window
                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    boolean focusable = true; // lets taps outside the popup also dismiss it
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                    // show the popup window
                    popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
                    Button add_btn = popupView.findViewById(R.id.add_btn);

                    add_btn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "add_btn_Clicked");
                            String component = componentEditText.getText().toString();
                            Allergy allergy = new Allergy(component);

                            mReference.child("USERS")
                                    .child(mAuth.getCurrentUser().getUid().toString())
                                    .child("components")
                                    .push().setValue(allergy);
                        }
                    });

                    dimBehind(popupWindow);
                    // dismiss the popup window when touched
                    popupView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            popupWindow.dismiss();
                            return true;
                        }
                    });
                    break;
            }
        }
    };

    public static void dimBehind(PopupWindow popupWindow) {
        View container;
        if (popupWindow.getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent();
            } else {
                container = popupWindow.getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent().getParent();
            } else {
                container = (View) popupWindow.getContentView().getParent();
            }
        }
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);
    }


    public void startGalleryChooser() {
        Log.d(TAG, "starting gallery");
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        Log.d(TAG, "starting camera");
        if (PermissionUtils.requestPermission(this, CAMERA_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    //바코드



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            intentPhotoUri=data.getData();
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intentPhotoUri=photoUri;
            uploadImage(photoUri);
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(result != null) {
            if(result.getContents() == null) {
                //Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                barcodeResult=result.getContents();
                getSnackDataByBarcode();

            }
        }

    }

    public void getSnackDataByBarcode(){
        String resultText = "";

        try {
            resultText = new Task().execute().get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Log.i("##",resultText);

    }

    public class TextTask extends AsyncTask<String, Void, String> {
        private final String KEY = "5bf53c4f-84cb-4522-b57f-1e83fa076ef0";
        private final String UID = "b3597253-362e-4c54-8a83-4e7a846c3681";
        private final String BASE_URL = "https://apis.eatsight.com/foodinfo/1.0/foods";
        private HttpURLConnection conn = null;
        private URL url = null;


        private String getResponseMsg(String resourceURL) throws IOException {
            String data="";
            String str="";
            url = new URL(BASE_URL +resourceURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("DS-ApplicationKey", KEY);
            conn.setRequestProperty("DS-AccessToken", UID);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader responseBodyReader = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(responseBodyReader);
                StringBuffer buffer = new StringBuffer();
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                data = buffer.toString();

                reader.close();
            }

            conn.disconnect();


            return data;
        }

        @Override
        protected String doInBackground(String... params) {
            String receiveMsg = "";
            Food food=new Food();
            try {
//                receiveMsg = getResponseMsg("?foodType=PFD&searchField=foodName&offset=0&limit=1&searchValue="+ visionResult);
                receiveMsg = getResponseMsg("?foodType=PFD&searchField=foodName&offset=0&limit=10&searchValue="+ "빼빼로");
                JSONArray jsonArray = new JSONObject(receiveMsg).getJSONArray("items");
                Log.d(TAG, "receiveMsg" + receiveMsg.toString());
                Log.d(TAG, "jsonArray" + jsonArray.toString());

                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String foodId = jsonObject.optString("foodId");
                if(!foodId.isEmpty()){
                    food = getFoodResult(new JSONObject(getResponseMsg("/"+foodId)));
                }

                Intent intent = new Intent(MainActivity.this,FoodResultActivity.class);
                intent.putExtra("food-result",food);
                startActivity(intent);


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return receiveMsg;
        }

        private Food getFoodResult(JSONObject jsonObject) throws JSONException, IOException {
            int count=0;

            Food food = new Food();
            food.setFoodId(jsonObject.optString("foodId"));
            food.setThumbnailUrl(jsonObject.optString("thumbnailUrl"));
            food.setBarcode(jsonObject.optString("barcode"));
            food.setFoodName(jsonObject.optString("foodName"));
            food.setTags(jsonObject.optString("tags"));
            food.setFoodClassifyId(jsonObject.optString("foodClassifyId"));
            food.setFoodClassifyName(jsonObject.optString("foodClassifyName"));

            List<FoodMaterial> foodMaterials = new ArrayList<>();
            JSONArray foodMaterialsJArray = jsonObject.optJSONArray("foodMaterials");
            if(foodMaterialsJArray!=null) {
                for (int i = 0; i < foodMaterialsJArray.length(); i++) {
                    JSONObject jObject = foodMaterialsJArray.getJSONObject(i);
                    FoodMaterial foodMaterial = new FoodMaterial();
                    String materialName = jObject.optString("materialName");
                    foodMaterial.setMaterialName(materialName);
                    foodMaterial.setMaterialStructure(jObject.optString("materialStructure"));
                    for(String my : allergies){
                        if(materialName.equals(my)&&!foodMaterial.isMyAllergy()) {
                            foodMaterial.setMyAllergy(true);
                            count++;
                        }
                    }
                    foodMaterials.add(foodMaterial);
                }
            }
            food.setFoodMaterials(foodMaterials);

            List<AllergyIngredient> allergyIngredients = new ArrayList<>();
            JSONArray allergyJArray = jsonObject.optJSONArray("allergyIngredient");
            if(allergyJArray!=null) {
                for (int j = 0; j < allergyJArray.length(); j++) {
                    JSONObject jObject = allergyJArray.getJSONObject(j);
                    AllergyIngredient allergyIngredient = new AllergyIngredient();
                    String materialId = jObject.optString("materialId");
                    allergyIngredient.setMaterialId(materialId);
                    String materialName = jObject.optString("materialName");
                    allergyIngredient.setMaterialName(materialName);

                    for(String my : allergies){
                        if(materialName.equals(my)&&!allergyIngredient.isMyAllergy()) {
                            allergyIngredient.setMyAllergy(true);
                            count++;
                        }
                    }
                    allergyIngredients.add(allergyIngredient);
                }
            }
            food.setAllergyIngredients(allergyIngredients);
            food.setCount(count);

            return food;
        }

    }

    public class Task extends AsyncTask<String, Void, String> {
        private final String KEY = "5bf53c4f-84cb-4522-b57f-1e83fa076ef0";
        private final String UID = "b3597253-362e-4c54-8a83-4e7a846c3681";
        private final String BASE_URL = "https://apis.eatsight.com/foodinfo/1.0/foods";
        private HttpURLConnection conn = null;
        private URL url = null;


        private String getResponseMsg(String resourceURL) throws IOException {
            String data="";
            String str="";
            url = new URL(BASE_URL +resourceURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("DS-ApplicationKey", KEY);
            conn.setRequestProperty("DS-AccessToken", UID);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader responseBodyReader = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(responseBodyReader);
                StringBuffer buffer = new StringBuffer();
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                data = buffer.toString();

                reader.close();
            }

            conn.disconnect();


            return data;
        }

        @Override
        protected String doInBackground(String... params) {
            String receiveMsg = "";
            Food food=new Food();
            try {
                receiveMsg = getResponseMsg("?foodType=PFD&searchField=barcode&offset=0&limit=1&searchValue="+barcodeResult);
                JSONArray jsonArray = new JSONObject(receiveMsg).getJSONArray("items");

                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String foodId = jsonObject.optString("foodId");
                if(!foodId.isEmpty()){
                    food = getFoodResult(new JSONObject(getResponseMsg("/"+foodId)));
                }

                Intent intent = new Intent(MainActivity.this,FoodResultActivity.class);
                intent.putExtra("food-result",food);
                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return receiveMsg;
        }

        private Food getFoodResult(JSONObject jsonObject) throws JSONException, IOException {
            int count=0;

            Food food = new Food();
            food.setFoodId(jsonObject.optString("foodId"));
            food.setThumbnailUrl(jsonObject.optString("thumbnailUrl"));
            food.setBarcode(jsonObject.optString("barcode"));
            food.setFoodName(jsonObject.optString("foodName"));
            food.setTags(jsonObject.optString("tags"));
            food.setFoodClassifyId(jsonObject.optString("foodClassifyId"));
            food.setFoodClassifyName(jsonObject.optString("foodClassifyName"));

            List<FoodMaterial> foodMaterials = new ArrayList<>();
            JSONArray foodMaterialsJArray = jsonObject.optJSONArray("foodMaterials");
            if(foodMaterialsJArray!=null) {
                for (int i = 0; i < foodMaterialsJArray.length(); i++) {
                    JSONObject jObject = foodMaterialsJArray.getJSONObject(i);
                    FoodMaterial foodMaterial = new FoodMaterial();
                    String materialName = jObject.optString("materialName");
                    foodMaterial.setMaterialName(materialName);
                    foodMaterial.setMaterialStructure(jObject.optString("materialStructure"));
                    for(String my : allergies){
                        if(materialName.equals(my)&&!foodMaterial.isMyAllergy()) {
                            foodMaterial.setMyAllergy(true);
                            count++;
                        }
                    }
                    foodMaterials.add(foodMaterial);
                }
            }
            food.setFoodMaterials(foodMaterials);

            List<AllergyIngredient> allergyIngredients = new ArrayList<>();
            JSONArray allergyJArray = jsonObject.optJSONArray("allergyIngredient");
            if(allergyJArray!=null) {
                for (int j = 0; j < allergyJArray.length(); j++) {
                    JSONObject jObject = allergyJArray.getJSONObject(j);
                    AllergyIngredient allergyIngredient = new AllergyIngredient();
                    String materialId = jObject.optString("materialId");
                    allergyIngredient.setMaterialId(materialId);
                    String materialName = jObject.optString("materialName");
                    allergyIngredient.setMaterialName(materialName);

                    for(String my : allergies){
                        if(materialName.equals(my)&&!allergyIngredient.isMyAllergy()) {
                            allergyIngredient.setMyAllergy(true);
                            count++;
                        }
                    }
                    allergyIngredients.add(allergyIngredient);
                }
            }
            food.setAllergyIngredients(allergyIngredients);
            food.setCount(count);

            return food;
        }

    }



    //바코드 여기까지

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "requesting permission");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), uri), 1200);
                callCloudVision(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        //mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature disiredFeature = new Feature();
                            disiredFeature.setType("TEXT_DETECTION");
                            disiredFeature.setMaxResults(10);
                            add(disiredFeature);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                Log.d(TAG, "got visionResult "+ visionResult);
                visionResult = result;
                try {
                    new TextTask().execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
//                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
//                intent.putExtra("result", result);
//                intent.putExtra("PhotoURI", intentPhotoUri);
//                intent.putStringArrayListExtra("allergies", allergies);
//                startActivity(intent);
            }
        }.execute();
    }

    public static Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        if (response.getResponses().get(0).getFullTextAnnotation() == null)
            return "";

        TextAnnotation texts = response.getResponses().get(0).getFullTextAnnotation();

        return texts.getText();
    }

    private void initFirebaseDatabase() {
        mUserReference = mFirebaseDatabase.getReference("USERS")
                .child(mAuth.getCurrentUser().getUid())
                .child("components");
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,"datasnapshot" + dataSnapshot.getValue());
                Allergy allergy = dataSnapshot.getValue(Allergy.class);
                if (allergy != null) {
                    allergy.setFirebaseKey(dataSnapshot.getKey());
                }
                mAdapter.add(allergy);
                allergies.add(allergy.getName());
                mListView.smoothScrollToPosition(mAdapter.getCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }


            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String firebaseKey = dataSnapshot.getKey();
                int count = mAdapter.getCount();
                for (int i = 0; i < count; i++) {
                    if (mAdapter.getItem(i).getFirebaseKey().equals(firebaseKey)) {
                        Allergy position = mAdapter.getItem(i);
                        mAdapter.remove(position);
                        allergies.remove(position.getName());
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mUserReference.addChildEventListener(mChildEventListener);
    }

    /**
     * Setup the firebase auth object
     */

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if(user == null){
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void signOut() {
        // Firebase sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                });
        mAuth.signOut();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}