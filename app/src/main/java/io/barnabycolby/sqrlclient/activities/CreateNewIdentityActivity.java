package io.barnabycolby.sqrlclient.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;;
import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import io.barnabycolby.sqrlclient.R;
import io.barnabycolby.sqrlclient.exceptions.RawUnsupportedException;
import io.barnabycolby.sqrlclient.sqrl.EntropyCollector;

import java.util.Arrays;
import java.util.List;

/**
 * Activity used to allow the user to create a new SQRL identity.
 *
 * The activity harvests entropy from the camera and uses it to create a new SQRL identity.
 */
public class CreateNewIdentityActivity extends AppCompatActivity {
    private int CAMERA_PERMISSION_REQUEST = 0;

    private String mUnrecoverableErrorKey = "error";
    private String mErrorStringKey = "errorString";
    private boolean mUnrecoverableErrorOccurred = false;
    private CameraManager mCameraManager;
    private String[] mCameraIds;
    private CameraDevice mCamera;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraSession;
    private CreateNewIdentityStateFragment mStateFragment;
    private String mStateFragmentTag = "stateFragment";
    private Surface mPreviewSurface;
    private Surface mEntropySurface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Standard Android stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_identity);

        FragmentManager fragmentManager = this.getFragmentManager();
        Fragment stateFragmentBeforeCast = fragmentManager.findFragmentByTag(mStateFragmentTag);
        if (stateFragmentBeforeCast == null) {
            this.mStateFragment = new CreateNewIdentityStateFragment();
            this.getFragmentManager().beginTransaction().add(this.mStateFragment, this.mStateFragmentTag).commit();
        } else {
            this.mStateFragment = (CreateNewIdentityStateFragment)stateFragmentBeforeCast;
        }

        // Restore the value of mUnrecoverableErrorOccurred if it exists
        if (savedInstanceState != null) {
            this.mUnrecoverableErrorOccurred = savedInstanceState.getBoolean(this.mUnrecoverableErrorKey, false);
        }

        if (this.mUnrecoverableErrorOccurred) {
            restore(savedInstanceState);
        } else {
            initialise();
        }
    }

    private void initialise() {
        // Get the camera manager
        this.mCameraManager = (CameraManager)this.getSystemService(Context.CAMERA_SERVICE);
        if (mCameraManager == null) {
            displayErrorMessage(R.string.camera_service_not_supported);
            return;
        }

        initialiseCameraPreview();
    }
    
    private void initialiseCameraPreview() {
        try {
            // Check if the system has any cameras
            mCameraIds = mCameraManager.getCameraIdList();
            if (mCameraIds.length == 0) {
                displayErrorMessage(R.string.no_cameras);
                return;
            }

            // Check for, and possibly request, permission to use the camera
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                onCameraPermissionGranted();
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            }
        } catch (CameraAccessException ex) {
            displayErrorMessage(ex.getMessage());
            return;
        }
    }

    private void restore(Bundle savedInstanceState) {
        String errorMessage = savedInstanceState.getString(this.mErrorStringKey);
        displayErrorMessage(errorMessage);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(this.mUnrecoverableErrorKey, this.mUnrecoverableErrorOccurred);
        if (this.mUnrecoverableErrorOccurred) {
            TextView errorTextView = (TextView)this.findViewById(R.id.ErrorTextView);
            outState.putString(this.mErrorStringKey, errorTextView.getText().toString());
        }
    }

    private void displayErrorMessage(int stringId) {
        String errorMessage = this.getResources().getString(stringId);
        displayErrorMessage(errorMessage);
    }

    private void displayErrorMessage(String errorMessage) {
        // Hide the main content
        View mainContent = this.findViewById(R.id.MainContent);
        mainContent.setVisibility(View.GONE);

        // Display the error message
        TextView errorTextView = (TextView)this.findViewById(R.id.ErrorTextView);
        errorTextView.setText(errorMessage);
        errorTextView.setVisibility(View.VISIBLE);

        // Set the error flag so that instance state is handled correctly
        this.mUnrecoverableErrorOccurred = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onCameraPermissionGranted();
        } else {
            displayErrorMessage(R.string.camera_permission_not_granted);
            return;
        }
    }

    /**
     * Called when permission to use the camera has been granted.
     */
    private void onCameraPermissionGranted() {
        TextureView cameraPreview = (TextureView)findViewById(R.id.CameraPreview);
        cameraPreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                surfaceTextureAvailable(surfaceTexture);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return true;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {}

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}
        });
    }

    /**
     * Called when a surface texture is available.
     */
    private void surfaceTextureAvailable(final SurfaceTexture surfaceTexture) {
        try {
            // Attempt to open a camera
            mCameraManager.openCamera(mCameraIds[0], new CameraDevice.StateCallback() {
                @Override
                public void onDisconnected(CameraDevice camera) {
                    displayErrorMessage(R.string.camera_disconnected);
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    displayErrorMessage(R.string.camera_error_occurred);
                }

                @Override
                public void onOpened(CameraDevice camera) {
                    mCamera = camera;
                    onCameraOpened(surfaceTexture);
                }
            }, null);
        } catch (CameraAccessException ex) {
            displayErrorMessage(ex.getMessage());
            return;
        }
    }

    /**
     * Called when a camera device has been opened successfully.
     */
    private void onCameraOpened(SurfaceTexture surfaceTexture) {
        CameraCharacteristics characteristics;
        try {
            characteristics = this.mCameraManager.getCameraCharacteristics(this.mCamera.getId());
            this.mStateFragment.setEntropyCollector(new EntropyCollector(characteristics));
        } catch (CameraAccessException | RawUnsupportedException ex) {
            displayErrorMessage(ex.getMessage());
            return;
        }

        // Set the texture buffer size so that the image capture is more performant
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = map.getOutputSizes(surfaceTexture.getClass());
        surfaceTexture.setDefaultBufferSize(sizes[0].getWidth(), sizes[0].getHeight());

        this.mPreviewSurface = new Surface(surfaceTexture);
        this.mEntropySurface = this.mStateFragment.getEntropyCollector().getSurface();
        List<Surface> surfaces = Arrays.asList(this.mPreviewSurface, this.mEntropySurface);

        try {
            this.mCamera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    displayErrorMessage(R.string.camera_configuration_failed);
                }

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mCameraSession = session;
                    onCameraSessionCreated();
                }

                @Override
                public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
                    onCameraSessionPrepared();
                }
            }, null);
        } catch (CameraAccessException ex) {
            displayErrorMessage(ex.getMessage());
            return;
        }
    }

    /**
     * Called once a camera session has been successfully created.
     */
    private void onCameraSessionCreated() {
        try {
            // Prepare the session buffers for use with the preview surface
            this.mCameraSession.prepare(mPreviewSurface);
        } catch (CameraAccessException ex) {
            displayErrorMessage(ex.getMessage());
            return;
        }
    }

    /**
     * Called once the camera session has been created and prepared.
     */
    private void onCameraSessionPrepared() {
        try {
            // Build the capture request
            CaptureRequest.Builder captureRequestBuilder;
            captureRequestBuilder = this.mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(mPreviewSurface);
            captureRequestBuilder.addTarget(mEntropySurface);
            this.mCaptureRequest = captureRequestBuilder.build();

            // Perform the capture
            startCameraCapture();
        } catch (CameraAccessException ex) {
            displayErrorMessage(ex.getMessage());
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.mCameraSession != null) {
            try {
                this.mCameraSession.stopRepeating();
            } catch (CameraAccessException ex) {
                // An exception may occur here if the camera is disconnected or has encountered a fatal error
                // In this scenario, we deal with the error when the app resumes
                // We do this to ensure that onPause remains lightweight
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraCapture();
    }

    private void startCameraCapture() {
        if (mCameraSession != null && mCaptureRequest != null) {
            try {
                mCameraSession.setRepeatingRequest(mCaptureRequest, null, null);
            } catch (CameraAccessException ex) {
                displayErrorMessage(ex.getMessage());
                return;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mCameraSession != null) {
            mCameraSession.close();
            mCameraSession = null;
        }

        if (mCamera != null) {
            mCamera.close();
            mCamera = null;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initialiseCameraPreview();
    }
}
