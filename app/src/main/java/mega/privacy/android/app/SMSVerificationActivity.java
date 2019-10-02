package mega.privacy.android.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import mega.privacy.android.app.lollipop.CountryCodePickerActivityLollipop;
import mega.privacy.android.app.lollipop.PinActivityLollipop;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaStringList;
import nz.mega.sdk.MegaStringListMap;

import static mega.privacy.android.app.lollipop.CountryCodePickerActivityLollipop.*;
import static mega.privacy.android.app.lollipop.LoginFragmentLollipop.*;
import static mega.privacy.android.app.utils.Constants.*;
import static mega.privacy.android.app.utils.LogUtil.*;
import static mega.privacy.android.app.utils.Util.*;

public class SMSVerificationActivity extends PinActivityLollipop implements View.OnClickListener, MegaRequestListenerInterface {
    
    public static final String SELECTED_COUNTRY_CODE = "COUNTRY_CODE";
    public static final String ENTERED_PHONE_NUMBER = "ENTERED_PHONE_NUMBER";
    private TextView helperText, selectedCountry, errorInvalidCountryCode, errorInvalidPhoneNumber, titleCountryCode, titlePhoneNumber, notNowButton;
    private View divider1, divider2;
    private ImageView errorInvalidPhoneNumberIcon;
    private RelativeLayout countrySelector;
    private EditText phoneNumberInput;
    private Button nextButton;
    private boolean isSelectedCountryValid, isPhoneNumberValid, isUserLocked, shouldDisableNextButton;
    private String selectedCountryCode, selectedCountryName, selectedDialCode;
    private ArrayList<String> countryCodeList;
    private boolean pendingSelectingCountryCode = false;
    private String inferredCountryCode;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logDebug("SMSVerificationActivity onCreate");
        super.onCreate(savedInstanceState);
        MegaApplication.smsVerifyShowed(true);
        setContentView(R.layout.activity_sms_verification);
        Intent intent = getIntent();
        if (intent != null) {
            isUserLocked = intent.getBooleanExtra(NAME_USER_LOCKED,false);
        }
        logDebug("is user locked " + isUserLocked);

        //divider
        divider1 = findViewById(R.id.verify_account_divider1);
        divider2 = findViewById(R.id.verify_account_divider2);
        
        //titles
        titleCountryCode = findViewById(R.id.verify_account_country_label);
        titlePhoneNumber = findViewById(R.id.verify_account_phone_number_label);
        selectedCountry = findViewById(R.id.verify_account_selected_country);

        TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        if(tm != null) {
            inferredCountryCode = tm.getNetworkCountryIso();
        }
        megaApi.getCountryCallingCodes(this);
        //set helper text
        helperText = findViewById(R.id.verify_account_helper);
        if (isUserLocked) {
            String text = getResources().getString(R.string.verify_account_helper_locked);
            helperText.setText(text);
        } else {
            boolean isAchievementUser = megaApi.isAchievementsEnabled();
            logDebug("is achievement user: " + isAchievementUser);
            if (isAchievementUser) {
                helperText.setText(R.string.sms_add_phone_number_dialog_msg_achievement_user);
            } else {
                helperText.setText(R.string.sms_add_phone_number_dialog_msg_non_achievement_user);
            }
        }

        //country selector
        countrySelector = findViewById(R.id.verify_account_country_selector);
        countrySelector.setOnClickListener(this);
        
        //phone number input
        phoneNumberInput = findViewById(R.id.verify_account_phone_number_input);
        phoneNumberInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v,int actionId,KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    nextButtonClicked();
                    return true;
                }
                return false;
            }
        });
        phoneNumberInput.setImeActionLabel(getString(R.string.general_create),EditorInfo.IME_ACTION_DONE);
        phoneNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {
            
            }
            
            @Override
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                errorInvalidPhoneNumber.setVisibility(View.GONE);
                errorInvalidPhoneNumberIcon.setVisibility(View.GONE);
                divider2.setBackgroundColor(Color.parseColor("#8A000000"));
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                int inputLength = input == null ? 0 : input.length();
                if (inputLength > 0) {
                    titlePhoneNumber.setTextColor(Color.parseColor("#FF00BFA5"));
                    titlePhoneNumber.setVisibility(View.VISIBLE);
                } else {
                    phoneNumberInput.setHint(R.string.verify_account_phone_number_placeholder);
                    titlePhoneNumber.setVisibility(View.GONE);
                }
            }
        });
        
        //buttons
        nextButton = findViewById(R.id.verify_account_next_button);
        nextButton.setOnClickListener(this);
        
        notNowButton = findViewById(R.id.verify_account_not_now_button);
        notNowButton.setOnClickListener(this);
        
        if(isUserLocked){
            notNowButton.setVisibility(View.GONE);
        }else{
            notNowButton.setVisibility(View.VISIBLE);
        }
        
        //error message and icon
        errorInvalidCountryCode = findViewById(R.id.verify_account_invalid_country_code);
        errorInvalidPhoneNumber = findViewById(R.id.verify_account_invalid_phone_number);
        errorInvalidPhoneNumberIcon = findViewById(R.id.verify_account_invalid_phone_number_icon);
        
        //set saved state
        if(savedInstanceState != null){
            selectedCountryCode = savedInstanceState.getString(COUNTRY_CODE);
            selectedCountryName = savedInstanceState.getString(COUNTRY_NAME);
            selectedDialCode = savedInstanceState.getString(DIAL_CODE);
    
            if(selectedCountryCode != null && selectedCountryName != null && selectedDialCode != null){
                String label = selectedCountryName + " (" + selectedDialCode + ")";
                selectedCountry.setText(label);
                errorInvalidCountryCode.setVisibility(View.GONE);
                titleCountryCode.setVisibility(View.VISIBLE);
                titleCountryCode.setTextColor(Color.parseColor("#FF00BFA5"));
                selectedCountry.setTextColor(Color.parseColor("#DE000000"));
                divider1.setBackgroundColor(Color.parseColor("#8A000000"));
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        logDebug("onBackPressed");
        if(isUserLocked){
            return;
        }
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MegaApplication.smsVerifyShowed(false);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.verify_account_country_selector): {
                logDebug("verify_account_country_selector clicked");
                if (this.countryCodeList != null) {
                    launchCountryPicker();
                } else {
                    logDebug("Country code is not loaded");
                    megaApi.getCountryCallingCodes(this);
                    pendingSelectingCountryCode = true;
                }
                break;
            }
            case (R.id.verify_account_next_button): {
                logDebug("verify_account_next_button clicked");
                nextButtonClicked();
                break;
            }
            case (R.id.verify_account_not_now_button):{
                logDebug("verify_account_not_now_button clicked");
                finish();
                break;
            }
            default:
                break;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == Constants.REQUEST_CODE_COUNTRY_PICKER && resultCode == RESULT_OK) {
            logDebug("onActivityResult REQUEST_CODE_COUNTRY_PICKER OK");
            selectedCountryCode = data.getStringExtra(COUNTRY_CODE);
            selectedCountryName = data.getStringExtra(COUNTRY_NAME);
            selectedDialCode = data.getStringExtra(DIAL_CODE);
            
            String label = selectedCountryName + " (" + selectedDialCode + ")";
            selectedCountry.setText(label);
            errorInvalidCountryCode.setVisibility(View.GONE);
            titleCountryCode.setVisibility(View.VISIBLE);
            titleCountryCode.setTextColor(Color.parseColor("#FF00BFA5"));
            selectedCountry.setTextColor(Color.parseColor("#DE000000"));
            divider1.setBackgroundColor(Color.parseColor("#8A000000"));
        } else if (requestCode == Constants.REQUEST_CODE_VERIFY_CODE && resultCode == RESULT_OK) {
            logDebug("onActivityResult REQUEST_CODE_VERIFY_CODE OK");
            setResult(RESULT_OK);
            finish();
        }
    }
    
    private void launchCountryPicker() {
        logDebug("launchCountryPicker");
        Intent intent = new Intent(getApplicationContext(),CountryCodePickerActivityLollipop.class);
        intent.putStringArrayListExtra("country_code", this.countryCodeList);
        startActivityForResult(intent, Constants.REQUEST_CODE_COUNTRY_PICKER);
    }
    
    private void nextButtonClicked() {
        logDebug("nextButtonClicked");
        hideKeyboard(this);
        hideError();
        validateFields();
        if (isPhoneNumberValid && isSelectedCountryValid) {
            logDebug("nextButtonClicked no error");
            hideError();
            RequestTxt();
        } else {
            showCountryCodeValidationError();
            showPhoneNumberValidationError(null);
        }
    }
    
    private void validateFields() {
        logDebug("validateFields");
        //validate phone number
        String phoneNumber = PhoneNumberUtils.formatNumberToE164(phoneNumberInput.getText().toString(),selectedCountryCode);
        if(phoneNumber != null){
            isPhoneNumberValid = true;
        }else{
            phoneNumberInput.setHint("");
            isPhoneNumberValid = false;
        }
        
        if (selectedDialCode != null && selectedDialCode.length() >= 3) {
            isSelectedCountryValid = true;
        } else {
            isSelectedCountryValid = false;
        }
        
        logDebug("validateFields isSelectedCountryValid " + isSelectedCountryValid + " isPhoneNumberValid " + isPhoneNumberValid);
    }
    
    private void showCountryCodeValidationError() {
        if (!isSelectedCountryValid) {
            if(selectedDialCode == null) {
                selectedCountry.setText("");
            } else {
                selectedCountry.setText(R.string.verify_account_counry_label);
            }
            logDebug("show invalid country error");
            errorInvalidCountryCode.setVisibility(View.VISIBLE);
            titleCountryCode.setVisibility(View.VISIBLE);
            titleCountryCode.setTextColor(Color.parseColor("#FFFF333A"));
            divider1.setBackgroundColor(Color.parseColor("#FFFF333A"));
        }
    }
    
    private void showPhoneNumberValidationError(String errorMessage) {
        if (!isPhoneNumberValid) {
            logDebug("show invalid phone number error");
            errorInvalidPhoneNumber.setVisibility(View.VISIBLE);
            errorInvalidPhoneNumberIcon.setVisibility(View.VISIBLE);
            titlePhoneNumber.setVisibility(View.VISIBLE);
            titlePhoneNumber.setTextColor(Color.parseColor("#FFFF333A"));
            divider2.setBackgroundColor(Color.parseColor("#FFFF333A"));
            if (errorMessage != null) {
                errorInvalidPhoneNumber.setText(errorMessage);
            }
        }
    }
    
    private void hideError() {
        logDebug("hide Errors");
        errorInvalidCountryCode.setVisibility(View.GONE);
        errorInvalidPhoneNumber.setVisibility(View.GONE);
        errorInvalidPhoneNumberIcon.setVisibility(View.GONE);
        titleCountryCode.setTextColor(Color.parseColor("#FF00BFA5"));
        titlePhoneNumber.setTextColor(Color.parseColor("#FF00BFA5"));
        divider1.setBackgroundColor(Color.parseColor("#8A000000"));
        divider2.setBackgroundColor(Color.parseColor("#8A000000"));
    }
    
    private void RequestTxt() {
        logDebug("RequestTxt shouldDisableNextButton is " + shouldDisableNextButton);
        if(!shouldDisableNextButton){
            nextButton.setBackground(getDrawable(R.drawable.background_button_disable));
            String phoneNumber = PhoneNumberUtils.formatNumberToE164(phoneNumberInput.getText().toString(),selectedCountryCode);
            logDebug(" RequestTxt phone number is " + phoneNumber);
            shouldDisableNextButton = true;
            megaApi.sendSMSVerificationCode(phoneNumber,this);
        }
    }
    
    @Override
    public void onRequestStart(MegaApiJava api,MegaRequest request) {
    
    }
    
    @Override
    public void onRequestUpdate(MegaApiJava api,MegaRequest request) {
    
    }
    
    @Override
    public void onRequestFinish(MegaApiJava api,MegaRequest request,MegaError e) {
        shouldDisableNextButton = false;
        nextButton.setBackground(getDrawable(R.drawable.background_accent_button));
        nextButton.setTextColor(Color.WHITE);
        if (request.getType() == MegaRequest.TYPE_SEND_SMS_VERIFICATIONCODE) {
            logDebug("send phone number,get code" + e.getErrorCode());
            if (e.getErrorCode() == MegaError.API_OK) {
                logDebug("will receive sms");
                String enteredPhoneNumber = phoneNumberInput.getText().toString();
                Intent intent = new Intent(this,SMSVerificationReceiveTxtActivity.class);
                intent.putExtra(SELECTED_COUNTRY_CODE,selectedDialCode);
                intent.putExtra(ENTERED_PHONE_NUMBER,enteredPhoneNumber);
                intent.putExtra(NAME_USER_LOCKED,isUserLocked);
                startActivityForResult(intent,REQUEST_CODE_VERIFY_CODE);
            } else if (e.getErrorCode() == MegaError.API_ETEMPUNAVAIL) {
                logDebug("reached limitation.");
                errorInvalidPhoneNumber.setVisibility(View.VISIBLE);
                errorInvalidPhoneNumber.setTextColor(Color.parseColor("#FFFF333A"));
                errorInvalidPhoneNumber.setText(R.string.verify_account_error_reach_limit);
            } else if (e.getErrorCode() == MegaError.API_EACCESS) {
                logDebug("already verified");
                isPhoneNumberValid = false;
                String errorMessage = getResources().getString(R.string.verify_account_invalid_phone_number);
                showPhoneNumberValidationError(errorMessage);
            }else if (e.getErrorCode() == MegaError.API_EARGS) {
                logDebug("Invalid phone number");
                isPhoneNumberValid = false;
                String errorMessage = getResources().getString(R.string.verify_account_invalid_phone_number);
                showPhoneNumberValidationError(errorMessage);
            } else if (e.getErrorCode() == MegaError.API_EEXIST) {
                logDebug("Phone number has been registered");
                isPhoneNumberValid = false;
                String errorMessage = getResources().getString(R.string.verify_account_error_phone_number_register);
                showPhoneNumberValidationError(errorMessage);
            } else {
                logDebug("sms TYPE_SEND_SMS_VERIFICATIONCODE " + e.getErrorString());
                isPhoneNumberValid = false;
                String errorMessage = getResources().getString(R.string.verify_account_invalid_phone_number);
                showPhoneNumberValidationError(errorMessage);
            }
        }

        if (request.getType() == MegaRequest.TYPE_GET_COUNTRY_CALLING_CODES) {
            if (e.getErrorCode() == MegaError.API_OK) {
                ArrayList<String> codedCountryCode = new ArrayList<>();
                MegaStringListMap listMap = request.getMegaStringListMap();
                MegaStringList keyList = listMap.getKeys();
                for (int i = 0; i < keyList.size(); i++) {
                    String key = keyList.get(i);
                    StringBuffer contentBuffer = new StringBuffer();
                    contentBuffer.append(key + ":");
                    for (int j = 0; j < listMap.get(key).size(); j++) {
                        String dialCode = listMap.get(key).get(j);
                        if(key.equalsIgnoreCase(inferredCountryCode)) {
                            Locale locale = new Locale("", inferredCountryCode);
                            selectedCountryName = locale.getDisplayName();
                            selectedCountryCode = key;
                            selectedDialCode = "+" + dialCode;

                            String label = selectedCountryName + " (" + selectedDialCode + ")";
                            selectedCountry.setText(label);
                        }
                        contentBuffer.append(dialCode + ",");
                    }
                    codedCountryCode.add(contentBuffer.toString());
                }
                this.countryCodeList = codedCountryCode;
                if (pendingSelectingCountryCode) {
                    launchCountryPicker();
                    pendingSelectingCountryCode = false;
                }
            } else {
                logDebug("the country code is not responded correctly");
                Util.showSnackBar(this, Constants.SNACKBAR_TYPE, getString(R.string.verify_account_not_loading_country_code), -1);
            }
        }
    }
    
    @Override
    public void onRequestTemporaryError(MegaApiJava api,MegaRequest request,MegaError e) {
    
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence(COUNTRY_CODE, selectedCountryCode);
        outState.putCharSequence(COUNTRY_NAME, selectedCountryName);
        outState.putCharSequence(DIAL_CODE, selectedDialCode);
        super.onSaveInstanceState(outState);
    }
}
