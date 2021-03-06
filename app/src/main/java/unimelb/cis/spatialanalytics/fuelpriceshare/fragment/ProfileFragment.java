package unimelb.cis.spatialanalytics.fuelpriceshare.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import unimelb.cis.spatialanalytics.fuelpriceshare.R;
import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigConstant;
import unimelb.cis.spatialanalytics.fuelpriceshare.config.ConfigURL;
import unimelb.cis.spatialanalytics.fuelpriceshare.data.UserCookie;
import unimelb.cis.spatialanalytics.fuelpriceshare.data.Users;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.AppController;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.CustomRequest;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.MultiPartRequest;
import unimelb.cis.spatialanalytics.fuelpriceshare.http.MyExceptionHandler;
import unimelb.cis.spatialanalytics.fuelpriceshare.maps.DrawOnMap.DrawMarkersOnMap;
import unimelb.cis.spatialanalytics.fuelpriceshare.others.ImageDecoder;
import unimelb.cis.spatialanalytics.fuelpriceshare.others.ImagePicker;
import unimelb.cis.spatialanalytics.fuelpriceshare.others.RandomGenerateUniqueIDs;

/**
 * Created by hanl4 on 17/02/2015.
 * modify user profile information including: birth, gender, first name, last name, what's up status, profile image.
 * Log out function is implemented.
 */
public class ProfileFragment extends Fragment {


     /*
   UI component parameters
    */

    private TextView textViewUserId;
    private TextView textViewUserName;
    private TextView textViewFirstName;
    private TextView textViewLastName;
    private TextView textViewGender;
    private TextView textViewBirth;
    private TextView textViewWhatUp;
    public static TextView textViewCredit;

    private Button buttonLotOut;

    private ImageView imageViewProfiePhoto;

    private LinearLayout linearLayoutProfilePhoto;
    private LinearLayout linearLayoutPassword;
    private LinearLayout linearLayoutUsername;
    private LinearLayout linearLayoutFirstName;
    private LinearLayout linearLayoutLastName;
    private LinearLayout linearLayoutGender;
    private LinearLayout linearLayoutBirth;
    private LinearLayout linearLayoutWhatUp;
    private LinearLayout linearLayoutCredit;

    /**
     * Select image by camera or local files (photos, library, folder)
     */
    private ImagePicker imagePicker;


    /**
     * For activity result
     */

    private final int REQUEST_CAMERA = ConfigConstant.REQUEST_CAMERA;
    private final int SELECT_FILE = ConfigConstant.SELECT_FILE;


    /**
     * User attributes
     */

    private Bitmap bitmapProfileImage;


    /**
     * User information editing dialog panel layout and views
     */
    private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    private LinearLayout.LayoutParams paramsHeightWrapContent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    /**
     * Capture whether profile changes or not; if changes, then upload to server
     */

    private boolean isModify = false;
    private boolean localFlag = true;
    private boolean isImageUploadingOrDownloading = false;//if we are uploading profile image to the server, it is set to be true.

    /**
     * Date picker
     */
    private Calendar calender = Calendar.getInstance();

    private final String BIRTH_FORMAT = ConfigConstant.BIRTH_FORMAT;


    /**
     * Local stored user information. used as session
     */
    private SharedPreferences pref;

    //For LOG
    private final String TAG = ProfileFragment.class.getSimpleName();


    public ProfileFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);


        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //setHasOptionsMenu(true);


        /* Initialize UI components */
        textViewUserName = (TextView) rootView.findViewById(R.id.profile_setting_username);
        textViewUserId = (TextView) rootView.findViewById(R.id.profile_setting_userid);
        textViewFirstName = (TextView) rootView.findViewById(R.id.profile_setting_firstname);
        textViewLastName = (TextView) rootView.findViewById(R.id.profile_setting_lastname);
        textViewGender = (TextView) rootView.findViewById(R.id.profile_setting_gender);
        textViewBirth = (TextView) rootView.findViewById(R.id.profile_setting_birth);
        textViewWhatUp = (TextView) rootView.findViewById(R.id.profile_setting_whatup);
        textViewCredit = (TextView) rootView.findViewById(R.id.profile_setting_credit);

        buttonLotOut = (Button) rootView.findViewById(R.id.btnLogout);
        buttonLotOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logMeOut(v);
            }
        });

        imageViewProfiePhoto = (ImageView) rootView.findViewById(R.id.profile_setting_profile_image);

        /* Set UI components default values*/
        setDefaultValues();


        linearLayoutProfilePhoto = (LinearLayout) rootView.findViewById(R.id.profile_setting_linear_layout_profile_photo);
        linearLayoutPassword = (LinearLayout) rootView.findViewById(R.id.profile_setting_linear_layout_password);
        linearLayoutUsername = (LinearLayout) rootView.findViewById(R.id.profile_setting_linear_layout_username);
        linearLayoutFirstName = (LinearLayout) rootView.findViewById(R.id.profile_setting_linear_layout_fristname);
        linearLayoutLastName = (LinearLayout) rootView.findViewById(R.id.profile_setting_linear_layout_lastname);
        linearLayoutBirth = (LinearLayout) rootView.findViewById(R.id.profile_setting_linear_layout_birth);
        linearLayoutGender = (LinearLayout) rootView.findViewById(R.id.profile_setting_linear_layout_gender);
        linearLayoutWhatUp = (LinearLayout) rootView.findViewById(R.id.profile_setting_linear_layout_whatup);
        linearLayoutCredit = (LinearLayout) rootView.findViewById(R.id.profile_setting_linear_layout_credit);


        imagePicker = new ImagePicker(getActivity(), "Change Profile Photo");

        /*
        Change profile photo
        */
        linearLayoutProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isImageUploadingOrDownloading)
                    imagePicker.selectImageBoth();


            }
        });


        /**
         * change password
         */
        linearLayoutPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ////change password
                String title = "Change Password";
                profilePasswordEditingPanel(title, -404);//do not need to re-set password in the panel

            }
        });


        /*
        change first name
         */
        linearLayoutFirstName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = "Change First Name";
                profileTextEditingPanel(title, R.id.profile_setting_firstname);


            }
        });

        /*
        change username
         */
        linearLayoutUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = "Change Username";
                profileTextEditingPanel(title, R.id.profile_setting_username);


            }
        });

        /*
        change last name
         */
        linearLayoutLastName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "Change Last Name";
                profileTextEditingPanel(title, R.id.profile_setting_lastname);

            }
        });


        /*
        change gender
         */
        linearLayoutGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "Change Gender";

                profileGenderEditingPanel(title);

            }
        });


        /*
        change birth
         */
        linearLayoutBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDatePickerDialog();


            }
        });

        /*
        change the status: what's up
         */
        linearLayoutWhatUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = "Change Status";
                profileTextEditingPanel(title, R.id.profile_setting_whatup);

            }
        });


        return rootView;

    }


    public void profilePasswordEditingPanel(String title, final int resourceID) {
        //inflate the view
        LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        /*
        Initialize the UI component within the layout
         */
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_profile_update_password, null);
        final EditText editText1 = (EditText) layout.findViewById(R.id.profile_setting_edit_password1);
        final EditText editText2 = (EditText) layout.findViewById(R.id.profile_setting_edit_password2);
        Button buttonCancel = (Button) layout.findViewById(R.id.profile_setting_edit_button_cancel);
        Button buttonUpdate = (Button) layout.findViewById(R.id.profile_setting_edit_button_update);
        final TextView textViewError = (TextView) layout.findViewById(R.id.profile_setting_error);

        textViewError.setText("");


        //build the alter dialog, and set the view of layout
        final AlertDialog dialog = buildProfileUpdateDialog(layout, title);

        //confirm button clicked
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str1 = editText1.getText().toString().trim();
                String str2 = editText2.getText().toString().trim();
                //field can not be null or empty
                if (str1 != null && !str1.equals("") && str2 != null && !str2.equals("")) {
                    /*
                    Get the modified information, re-set corresponding view values and update user information
                    by calling modifyUserProfile function.
                     */
                    try {
                        JSONObject jsonObject = Users.getUserJSON();
                        if (str1.equals(str2)) {
                            jsonObject.put(Users.KEY_PASSWORD, str1);
                            isModify = true;
                            modifyUserProfile(resourceID, jsonObject);
                            dialog.dismiss();
                        } else {
                            textViewError.setText("Two password inputs are not the same!");


                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                        dialog.dismiss();
                    }


                } else {
                    textViewError.setText("Field can not be empty");
                }

            }
        });

        //cancel button clicked
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


    }


    /**
     * Set default values for Views
     */
    public void setDefaultValues() {
        textViewUserName.setText(Users.userName);
        textViewUserId.setText(Users.id);
        textViewFirstName.setText(Users.firstName);
        textViewLastName.setText(Users.lastName);
        textViewGender.setText(Users.gender);
        textViewBirth.setText(Users.birth);
        textViewWhatUp.setText(Users.whatup);
        // Han and Yu 02/04/2015, we set the credit value only by the function updateCredit()
        // textViewCredit.setText( String.valueOf(Users.credit) );


        if (Users.bitmap != null)
            imageViewProfiePhoto.setImageBitmap(Users.bitmap);
            //bitmapProfileImage=Users.bitmap;
            //imageViewProfiePhoto.setImageBitmap(bitmapProfileImage);

        else {

            if (Users.profileImage.equals(""))
                imageViewProfiePhoto.setImageResource(R.drawable.ic_action_picture);

            else {
                /**
                 * Google Volley API to download profile photo from server.
                 * For more detailed information, please refer to the official document by
                 * http://developer.android.com/training/volley/index.html
                 */
                isImageUploadingOrDownloading = true;
                ImageLoader imageLoader = AppController.getInstance().getImageLoader();

                // If you are using normal ImageView
                imageLoader.get(Users.profileImage, new ImageLoader.ImageListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Image Load Error: " + error.getMessage());
                        isImageUploadingOrDownloading = false;
                        // MyExceptionHandler.presentError(TAG,"Can not show profile image",getActivity(),error);
                    }

                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                        if (response.getBitmap() != null) {
                            // load image into imageview
                            isImageUploadingOrDownloading = false;
                            imageViewProfiePhoto.setImageBitmap(response.getBitmap());
                            //imageViewProfiePhoto.setImageBitmap(Bitmap.createScaledBitmap(response.getBitmap(), ConfigConstant.PROFILE_IMAGE_WIDTH, ConfigConstant.PROFILE_IMAGE_HEIGHT, false));

                            Users.bitmap = response.getBitmap();
                        }
                    }
                });


                /**
                 * Here is another way of displaying image into ImageView with the option of placeholder for
                 * loader and error. The loader placeholder will be displayed until the image gets downloaded.
                 * If the image fails to download, the error placeholder will be displayed.
                 */
             /*   imageLoader.get(Users.profileImage, ImageLoader.getImageListener(
                                imageViewProfiePhoto, R.drawable.ic_action_download, R.drawable.ic_action_picture)
                );*/
            }
        }

    }

    /*
    Date picker dialog for changing birthday
     */
    public void showDatePickerDialog() {
        //data format e.g., "20/02/2012";
        String str = textViewBirth.getText().toString();


        DateFormat format = new SimpleDateFormat(BIRTH_FORMAT);
        Date date = null;

        if (str != null && !str.equals(""))
            try {
                date = format.parse(str);
                calender.setTime(date);

            } catch (ParseException e) {
                //e.printStackTrace();
                Log.e(TAG, "Error date format", e);

            }
        //build DataPickerDialog
        final DatePickerDialog picker = new DatePickerDialog(getActivity(), null,
                calender.get(Calendar.YEAR), calender.get(Calendar.MONTH), calender.get(Calendar.DAY_OF_MONTH));

        picker.setCancelable(true);
        picker.setCanceledOnTouchOutside(true);
        picker.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Change birthday!");
                        /*edit birth information*/

                        int year = picker.getDatePicker().getYear();
                        int day = picker.getDatePicker().getDayOfMonth();
                        int month = picker.getDatePicker().getMonth();
                        calender.set(Calendar.YEAR, year);
                        calender.set(Calendar.MONTH, month);
                        calender.set(Calendar.DAY_OF_MONTH, day);

                        String str = new SimpleDateFormat(BIRTH_FORMAT).format(calender.getTime());
                        isModify = true;
                        JSONObject jsonObject = Users.getUserJSON();
                        try {
                            jsonObject.put(Users.KEY_BIRTH, str);

                            //update user information after modification
                            modifyUserProfile(R.id.profile_setting_birth, jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                        }
                    }
                });
        picker.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        picker.show();


    }


    /**
     * builds a floating dialog to edit user's textural information including username, first name, last name, what's up status, etc.
     *
     * @param title      the title of the dialog
     * @param resourceID the id of view
     */

    public void profileTextEditingPanel(String title, final int resourceID) {
        //inflate the view
        LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        /*
        Initialize the UI component within the layout
         */
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_profile_update_edittext, null);
        final EditText editText = (EditText) layout.findViewById(R.id.profile_setting_edit_text);
        Button buttonCancel = (Button) layout.findViewById(R.id.profile_setting_edit_button_cancel);
        Button buttonUpdate = (Button) layout.findViewById(R.id.profile_setting_edit_button_update);
        final TextView textViewError = (TextView) layout.findViewById(R.id.profile_setting_error);

        textViewError.setText("");
        /*
        Set views default values
         */
        switch (resourceID) {
            case R.id.profile_setting_firstname:
                editText.setText(textViewFirstName.getText());
                break;
            case R.id.profile_setting_lastname:
                editText.setText(textViewLastName.getText());
                break;
            case R.id.profile_setting_whatup:
                editText.setText(textViewWhatUp.getText());
                break;
            case R.id.profile_setting_username:
                editText.setText(textViewUserName.getText());
                break;

            default:
                Log.e(TAG, "No such UI resource ID");
                return;


        }


        //build the alter dialog, and set the view of layout
        final AlertDialog dialog = buildProfileUpdateDialog(layout, title);

        //confirm button clicked
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = editText.getText().toString().trim();
                //field can not be null or empty
                if (str != null && !str.equals("")) {
                    /*
                    Get the modified information, re-set corresponding view values and update user information
                    by calling modifyUserProfile function.
                     */
                    try {
                        JSONObject jsonObject = Users.getUserJSON();
                        switch (resourceID) {
                            case R.id.profile_setting_firstname:
                                jsonObject.put(Users.KEY_FIRST_NAME, str);
                                isModify = true;
                                modifyUserProfile(resourceID, jsonObject);
                                break;
                            case R.id.profile_setting_lastname:
                                jsonObject.put(Users.KEY_LAST_NAME, str);
                                isModify = true;
                                modifyUserProfile(resourceID, jsonObject);
                                break;
                            case R.id.profile_setting_whatup:
                                jsonObject.put(Users.KEY_WHATUP, str);
                                isModify = true;
                                modifyUserProfile(resourceID, jsonObject);
                                break;

                            case R.id.profile_setting_username:
                                jsonObject.put(Users.KEY_USERNAME, str);
                                isModify = true;
                                modifyUserProfile(resourceID, jsonObject);
                                break;

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }

                    dialog.dismiss();
                } else {
                    textViewError.setText("Field can not be empty");
                }

            }
        });

        //cancel button clicked
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


    }


    /**
     * modify gender information
     *
     * @param title assign the title of the dialog
     */
    public void profileGenderEditingPanel(String title) {
        //inflate the layout from xml file
        LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        /*
        Initialize UI components within layout
         */
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_profile_update_gender, null);
        final TextView textViewMale = (TextView) layout.findViewById(R.id.profile_setting_edit_male);
        final TextView textViewFemale = (TextView) layout.findViewById(R.id.profile_setting_edit_female);
        ImageView iconMale = (ImageView) layout.findViewById(R.id.profile_setting_edit_icon_male);
        ImageView iconFemale = (ImageView) layout.findViewById(R.id.profile_setting_edit_icon_female);
        String gender = textViewGender.getText().toString().toLowerCase();

        /*
        Set up initial view values
         */
        if (gender.equals("male")) {
            iconMale.setVisibility(View.VISIBLE);
            iconFemale.setVisibility(View.GONE);
            localFlag = true;

        } else {
            iconMale.setVisibility(View.GONE);
            iconFemale.setVisibility(View.VISIBLE);
            localFlag = false;

        }
        //build the alter dialog, and set the view of layout
        final AlertDialog dialog = buildProfileUpdateDialog(layout, title);

        //male option is selected
        textViewMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Get the modified record and update both view and user information.
                 */
                String str = ((TextView) v).getText().toString();
                JSONObject jsonObject = Users.getUserJSON();
                try {
                    jsonObject.put(Users.KEY_GENDER, str);
                    dialog.dismiss();
                    if (!localFlag) {
                        isModify = true;
                        modifyUserProfile(R.id.profile_setting_gender, jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }
            }
        });

        //female option is selected
        textViewFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Get the modified record and update both view and user information.
                 */
                String str = ((TextView) v).getText().toString();
                JSONObject jsonObject = Users.getUserJSON();
                try {
                    jsonObject.put(Users.KEY_GENDER, str);
                    dialog.dismiss();
                    if (localFlag) {
                        isModify = true;
                        modifyUserProfile(R.id.profile_setting_gender, jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }

            }
        });


    }


    /**
     * Editing user information dialog
     *
     * @param view  the view of the dialog to present
     * @param title the title of the dialog
     * @return alter dialog
     */
    public AlertDialog buildProfileUpdateDialog(View view, String title) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        builder.setTitle(title);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        return dialog;


    }


    /**
     * Upload profile image to the server
     *
     * @param fileName   new image name with unique id
     * @param stringData string data to upload as well
     */

    public void uploadProfileImage2Server(String fileName, String stringData) {
        isImageUploadingOrDownloading = true;
        // Tag used to cancel the request
        String tag_json_obj = TAG;

        /**
         * Use google Volley lib to upload an image
         */

        MultiPartRequest multiPartRequest = new MultiPartRequest(ConfigURL.getUploadImageServlet(), bitmapProfileImage /*new File(profileImagePath)*/, fileName, null, stringData, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                isImageUploadingOrDownloading = false;
                if (response.has(ConfigConstant.KEY_ERROR)) {
                    Log.e(TAG, "profile image upload failed!" + response.toString());


                } else {
                    //if upload success, update local session
                    imageViewProfiePhoto.setImageBitmap(bitmapProfileImage);
                    //imageViewProfiePhoto.setImageBitmap(Bitmap.createScaledBitmap(bitmapProfileImage, ConfigConstant.PROFILE_IMAGE_WIDTH, ConfigConstant.PROFILE_IMAGE_HEIGHT, false));

                    Users.profileImage = Users.tempProfielImageName;
                    Users.tempProfielImageName = "";

                    //Profile Image was successfully uploaded to the server
                    isModify = false;
                    Users.bitmap = bitmapProfileImage;
                    UserCookie.storeUserLocal(pref);//update local session
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                isImageUploadingOrDownloading = false;
                MyExceptionHandler.presentError(TAG, "Update profile image failed!", getActivity(), error);

            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(multiPartRequest, tag_json_obj);

    }


    /**
     * Receive all the returning results of children activities. Detailed information please refer to the
     * official android programming document
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        String fileName, data;

        if (resultCode == getActivity().RESULT_OK) {

            switch (requestCode) {
                case REQUEST_CAMERA:

                    /*
                    Get the image captured by camera
                     */
                    File imageFile = imagePicker.getImageFile();
                    //compress the image
                    bitmapProfileImage = ImageDecoder.decodeSampledBitmapFromFile(imageFile, ConfigConstant.PROFILE_IMAGE_WIDTH, ConfigConstant.PROFILE_IMAGE_HEIGHT);
                    bitmapProfileImage = ImageDecoder.rotateImage(bitmapProfileImage, imageFile.getAbsolutePath());
                    bitmapProfileImage = ImageDecoder.createScaledBitmap(bitmapProfileImage, ConfigConstant.IMAGE_TYPE_PROFILE);

                    fileName = RandomGenerateUniqueIDs.getFileName("png");

                    data = Users.getUserJSONForImageUpload(ConfigURL.getServerProfileImageFolderBase() + fileName).toString();
                    uploadProfileImage2Server(fileName, data);


                    break;
                case SELECT_FILE:
                    /*
                    Get the image captured by selecting local file
                     */
                    if (intent != null) {
                        bitmapProfileImage = ImageDecoder.decodeSampledBitmapFromUri(getActivity(), intent.getData(), ConfigConstant.PROFILE_IMAGE_WIDTH, ConfigConstant.PROFILE_IMAGE_HEIGHT);
                        bitmapProfileImage = ImageDecoder.createScaledBitmap(bitmapProfileImage, ConfigConstant.IMAGE_TYPE_PROFILE);

                        fileName = RandomGenerateUniqueIDs.getFileName("png");

                        data = Users.getUserJSONForImageUpload(ConfigURL.getServerProfileImageFolderBase() + fileName).toString();
                        uploadProfileImage2Server(fileName, data);

                    } else {
                        Log.e(TAG, "No image file is selected");
                    }
                    break;
                default:
                    Log.e(TAG, "No such operation for switch");

                    break;

            }
        }
    }


    /**
     * Modify user information
     */
    public void modifyUserProfile(final int resourceID, final JSONObject updatedUserJson) {
        if (isModify) {

            /**
             * User Volley API developed by Google to handle the request!
             * Update couchDB data
             */

            // Tag used to cancel the request
            String tag_json_obj = TAG;


            Map<String, String> params = new HashMap<String, String>();
            params.put(ConfigConstant.KEY_COUCHDB_DOC_ACTION, "PUT");
            params.put(ConfigConstant.KEY_COUCHDB_DOC_ID, Users.getDocID());
            params.put(ConfigConstant.KEY_COUCHDB_DOC_DATA, updatedUserJson.toString());

            CustomRequest customRequest = new CustomRequest(ConfigURL.getCouchDBURL(), params, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                    if (response.has(ConfigConstant.KEY_ERROR)) {
                        Log.e(TAG, response.toString());
                    } else {
                        //if upload success, update local session
                        isModify = false;
                        updateModifiedInfo(resourceID, updatedUserJson);
                        UserCookie.storeUserLocal(pref);
                        Log.d(TAG, "Information Update Success");
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error: " + error.getMessage());
                    MyExceptionHandler.presentError(TAG, "Update profile information failed!", getActivity(), error);
                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(customRequest, tag_json_obj);

        }
    }


    public void updateModifiedInfo(int resourceID, JSONObject userJson) {
        try {
            String str;
            switch (resourceID) {
                case R.id.profile_setting_firstname:
                    str = userJson.getString(Users.KEY_FIRST_NAME);
                    textViewFirstName.setText(str);
                    Users.firstName = str;
                    break;
                case R.id.profile_setting_lastname:
                    str = userJson.getString(Users.KEY_LAST_NAME);
                    Users.lastName = str;
                    textViewLastName.setText(str);
                    break;
                case R.id.profile_setting_whatup:
                    str = userJson.getString(Users.KEY_WHATUP);
                    Users.whatup = str;
                    textViewWhatUp.setText(str);
                    break;

                case R.id.profile_setting_username:
                    str = userJson.getString(Users.KEY_USERNAME);
                    Users.userName = str;
                    textViewUserName.setText(str);
                    break;
                case R.id.profile_setting_birth:
                    str = userJson.getString(Users.KEY_BIRTH);
                    textViewBirth.setText(str);
                    Users.birth = str;
                    break;
                case R.id.profile_setting_gender:
                    str = userJson.getString(Users.KEY_GENDER);
                    textViewGender.setText(str);
                    Users.gender = str;
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Log out
     *
     * @param view please see the XML file for logout button
     */
    public void logMeOut(View view) {
        UserCookie.logOut(pref, getActivity());//clear local session

        ///// added by Yu Sun on 04/04/2015
        DrawMarkersOnMap.clearStations();
    }


    /**
     * show the latest information of the credit from the server user table.
     * Every time the ProfileFragment is visited, this function will be called.
     */
    public void updateCredit() {

        // Tag used to cancel the request
        String tag_json_obj = TAG + " : Fetch User Doc";

        Map<String, String> params = new HashMap<String, String>();
        params.put(ConfigConstant.KEY_COUCHDB_DOC_ACTION, "GET");
        params.put(ConfigConstant.KEY_COUCHDB_DOC_ID, Users.id);
        CustomRequest customRequest = new CustomRequest(ConfigURL.getCouchDBURL(), params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try {
                    //Update User information from the server to make sure that, the local stored user info is latest.
                    if (!response.has(ConfigConstant.KEY_ERROR)) {

                        if ( response.has(ConfigConstant.KEY_CREDIT) )
                        {

                            Users.credit = response.getInt(ConfigConstant.KEY_CREDIT);
                            textViewCredit.setText( String.valueOf(Users.credit) );
                        }
                        else
                        Log.e(TAG, "Can not get the field of Credit in user table");
                        UserCookie.storeUserLocal(pref);

                    } else
                        Log.e(TAG, "failed to update the local session from the server");

                } catch (JSONException e)

                {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }
            }


        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                MyExceptionHandler.presentError(TAG,
                        "read user table from server failed!", getActivity(), error);

            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(customRequest, tag_json_obj);

    }
}



