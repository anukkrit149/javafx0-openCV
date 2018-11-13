package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeUnit.*;

import java.util.concurrent.Executors;

public class Controller {

    @FXML
    private Button button;
    @FXML
    private ImageView currentFrame;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private VideoCapture capture = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    private static int cameraId = 0;




    @FXML
    public void startCAM(ActionEvent ob){

        if(!this.cameraActive){

            this.capture.open(cameraId);

            if (this.capture.isOpened()){

                this.cameraActive =true;
                /**
                 * here we created a thread through runnable interface for take data
                 * from webcam in Mat format and convert it into image for javafx usage
                 * and then update it in the application*/

                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {

                        Mat frame = grabFrame();
                        // convert and show the frame
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(currentFrame, imageToShow);


                    }
                } ;
                /**
                 * here schedule thread update image in ever 10 milliseconds
                 * from frameGrabber
                 * Stop camera stops the thread from updating image                 *
                 * */

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 10, TimeUnit.MILLISECONDS);

                this.button.setText("Stop Camera");

            }else {
                System.err.println("Camera Cannot be open");
            }
        }else {
            this.cameraActive = false;
            // update again the button content
            this.button.setText("Start Camera");

            // stop the timer
            this.stopAcquisition();

        }
    }
    /**
     * grabFrame function
     * */

    private Mat grabFrame() {
        Mat frame = new Mat();
//        Mat gray_frame = new Mat();

        if (this.capture.isOpened()){
            try {
                this.capture.read(frame);

//                if (!frame.empty()){
//                    Imgproc.cvtColor(frame,gray_frame,Imgproc.COLOR_BGR2GRAY);
//                }
            }catch (Exception e){
                System.out.println("Problem in Frame capturing" +
                        "e");
            }
        }
        return frame;

    }
    /**
     * stopAcquisition function
     * */
    private void stopAcquisition()
    {
        if (this.timer!=null && !this.timer.isShutdown())
        {
            try
            {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened())
        {
            // release the camera
            this.capture.release();
        }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view
     *            the {@link ImageView} to update
     * @param image
     *            the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image)
    {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    protected void setClosed()
    {
        this.stopAcquisition();
    }


}
