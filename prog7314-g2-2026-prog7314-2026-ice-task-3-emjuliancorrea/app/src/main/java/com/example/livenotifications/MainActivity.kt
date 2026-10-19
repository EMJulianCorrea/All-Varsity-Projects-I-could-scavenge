package com.talia.livenotificationdemogroup2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

/*

MyFirebaseMessagingService:
- getting the fb messages
- getting refreshed fcm tokens
- reading messages
- creating notifications
- building visible notifications
- handling messages when the activity is not visible

MainActivity:
- display the compose interface
- ask for notification permission
- show the notification permission status
- get the FCM token
- show the token FOR TESTING
 */

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "NotificationDemo"
    }

    //display current permission Status
    private var permissionStatus by mutableStateOf(
        "Notification permission has not been checked."
    )

    //store the current fcm registration token
    private var fcmToken by mutableStateOf<String?>(null)

    //stores an error message
    private var tokenError by mutableStateOf<String?>(null)

    /*
    ActivityResult API launcher is used to req notif permission on android 13+

    We don't display the dialog to req, android does that

    Callback runs after user chooses either Allow or Don't Allow
     */
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){
                isGranted ->
            if(isGranted){
                permissionStatus = "Notification permission granted."

                Log.d(TAG, "The user has given permission.")
            }else{
                permissionStatus = "Notification permission not granted."

                Log.w(TAG, "The user has denied permission.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        /*
         1: CHECK THE CURRENT NOTIFICATION PERMISSION

        Call the function that checks the current notification permission status.

        Why:
        - when the app opens, permissionStatus still contains the default message
        - the app must check whether notifications are already allowed
        - this is especially important if the user granted or denied permission
          during a previous session

        Function to call:
        updateNotificationPermissionStatus()
         */


        /*
         2: REQUEST THE CURRENT FCM TOKEN

        Call the function that retrieves the Firebase Cloud Messaging token.

        Why:
        - every installation of the app receives an FCM registration token
        - this token identifies this specific app installation
        - the token will be copied from Logcat and pasted into Firebase Console
          when sending a test notification
        - Firebase returns the token asynchronously, so the screen may appear
          before the token has been retrieved

        Function to call:
        retrieveFcmToken()
         */


        setContent {

            /*
             3: APPLY THE PROJECT THEME

            Wrap the Compose screen inside the theme that was generated when
            this Android Studio project was created.

            Find the correct theme composable inside:

            ui/theme/Theme.kt

            The name will probably be similar to:

            LiveNotificationDemoGroup2Theme

            Why:
            - applies the application's colours
            - applies Material 3 typography
            - supports light and dark mode
            - keeps the interface visually consistent
             */


            /*
             4: CREATE A SCAFFOLD

            Inside the theme, create a Scaffold.

            The Scaffold must:
            - fill the available screen
            - provide innerPadding to its screen content

            Why:
            - Scaffold provides a standard Material 3 screen structure
            - innerPadding helps prevent the content from overlapping
              the status bar, navigation bar or other system UI
             */


            /*
             5: CREATE THE MAIN COLUMN

            Inside the Scaffold content, create a Column.

            The Column should:
            - fill the available screen
            - apply innerPadding from Scaffold
            - apply additional screen padding
            - arrange components vertically
            - align the content neatly
            - allow vertical scrolling

            Why vertical scrolling is useful:
            - FCM tokens are very long
            - the token may not fit on a small phone screen
            - without scrolling, some buttons or messages may be cut off

            Compose components that may be required:
            - Column
            - Modifier.fillMaxSize()
            - Modifier.padding()
            - Modifier.verticalScroll()
            - rememberScrollState()
            - Arrangement
            - Alignment
             */


            /*
             6: DISPLAY A SCREEN HEADING

            Add a clear heading such as:

            Firebase Cloud Messaging

            Use an appropriate Material 3 typography style.

            Why:
            - communicates the purpose of the application
            - creates a clear visual hierarchy
             */


            /*
             7: DISPLAY A SHORT DESCRIPTION

            Add text explaining that this app receives real-time push
            notifications from Firebase.

            Example meaning:

            This application receives real-time push notifications
            from Firebase.

            Students may choose their own wording.
             */


            /*
             8: DISPLAY THE CURRENT PERMISSION STATUS

            Add a Text component that displays:

            permissionStatus

            Why:
            - permissionStatus is Compose state
            - whenever its value changes, Compose automatically updates the UI
            - users need to know whether notification permission is enabled
             */


            /*
             9: CREATE AN ENABLE NOTIFICATIONS BUTTON

            Add a button labelled something similar to:

            Enable Notifications

            When clicked, the button must call:

            askNotificationPermission()

            Why:
            - Android 13+ requires the user to approve notification permission
            - the application cannot display normal notifications until permission
              has been granted
            - Android 12 and lower do not require this runtime permission, but the
              existing function already handles those versions
             */


            /*
             10: DISPLAY AN FCM TOKEN HEADING

            Add a label similar to:

            FCM Registration Token:

            This heading should appear before the token value.
             */


            /*
             11: DISPLAY THE FCM TOKEN

            Add a Text component that displays:

            fcmToken

            If fcmToken is null, display a message such as:

            Retrieving token...

            Why:
            - Firebase token retrieval is asynchronous
            - the token may not be ready when the Compose screen first appears
            - using a fallback message gives the user feedback while waiting

            Hint:
            Use Kotlin's Elvis operator to provide the fallback value.
             */


            /*
             12: DISPLAY TOKEN ERRORS CONDITIONALLY

            Only display an error message if tokenError is not null.

            The error should:
            - use tokenError as its text
            - use the Material theme's error colour
            - have suitable spacing around it

            Why:
            - an error section should not take up space when no error exists
            - displaying the error helps diagnose Firebase configuration,
              internet or Google Play Services problems

            Hint:
            Use tokenError?.let { ... }
             */


            /*
             13: CREATE A REFRESH TOKEN BUTTON

            Add a button labelled something similar to:

            Refresh FCM Token

            When clicked, call:

            retrieveFcmToken()

            Why:
            - token retrieval may fail temporarily
            - internet access may be unavailable
            - Firebase configuration may have just been corrected
            - students need a simple way to request the token again
             */


            /*
             14: DISPLAY TESTING INSTRUCTIONS

            Add a short message explaining that the token should be copied
            from Logcat and pasted into Firebase Console when sending a test
            notification.

            Important:
            - the token is being displayed for classroom testing
            - production applications should normally send the token securely
              to a backend
            - the token should not be publicly shared
             */


            /*
             15: ADD APPROPRIATE SPACING

            Add Spacer components between:
            - the heading and description
            - the permission status and permission button
            - the permission section and token section
            - the token and error message
            - the token and refresh button
            - the refresh button and final instructions

            Why:
            - improves readability
            - prevents UI components from appearing cramped
            - creates a clear visual hierarchy
             */
        }
    }

    /*
    update permission status = message describing status
     */
    private fun updateNotificationPermissionStatus(){

        //android versions before 13 do not use the POST_NOTIFICATION runtime permission
        if(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ){
            permissionStatus = "Notification permission is automatically available on " +
                    "Android 12 and lower."

            return
        }

        //13+
        val permissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS

        ) == PackageManager.PERMISSION_GRANTED

        permissionStatus =
            if (permissionGranted)
            {
                "Notification permission granted."
            } else {
                "Notification permission not granted."
            }
    }

    //request permission on android 13+
    private fun askNotificationPermission(){

        //account for android 12 and lower
        if(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ){
            permissionStatus = "Notification permission is automatically available on " +
                    "Android 12 and lower."

            return
        }

        //check if permission is already granted
        val permissionGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS

            ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted){

            permissionStatus = "Permission granted."

            return
        }

        //some devices may recommend explaining why permissions are required,
        // esp if the user denies
        if(
            shouldShowRequestPermissionRationale(
                Manifest.permission.POST_NOTIFICATIONS
            )
        ){
            permissionStatus = "Permission to display notifications are needed so the app can show " +
                    "incoming Firebase messages."
        }

        //display androids permission dialog
        notificationPermissionLauncher.launch(
            Manifest.permission.POST_NOTIFICATIONS
        )

    }

    private fun retrieveFcmToken(){

        tokenError = null

        /*
        Firebase is asynchronous

        addOnCompleteListener runs after fb either return the token/ report and error
         */

        FirebaseMessaging
            .getInstance()
            .token
            .addOnCompleteListener { task ->
                if(!task.isSuccessful)
                {
                    tokenError = "Unable to get token."

                    Log.e(TAG,"Fetching the FCM registration token failed.",
                        task.exception
                    )
                    return@addOnCompleteListener
                }

                //token ids the installation of this app
                val token = task.result

                fcmToken = token

                //print the full token into logcat so we can copy it into firebase for TESTING ONLY
                Log.d(TAG,"FCM TOKEN: $token")
            }
    }

    override fun onResume() {
        super.onResume()

        //if the user turns off permissions, check again
        updateNotificationPermissionStatus()

    }
}