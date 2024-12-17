package Praktikum3;
/* RFTClientRcvThread.java
 Version 1.0
 Praktikum Rechnernetze HAW Hamburg
 Autor: M. Huebner
 */

import java.io.IOException;
import java.net.DatagramPacket;

public class RFTClientRcvThread extends Thread {
    /* Receive UDP packets and handle the ACKs */
    private RFTClient myRFTC;
    private long sendbase;

    public RFTClientRcvThread(RFTClient rftClient) {
        myRFTC = rftClient;
        sendbase = 0;
    }

    public void run() {
        RFTpacket ack;
        int dupCounter = 0; // count duplicate ACKs for fast retransmit
        DatagramPacket receivePacket;
        byte[] receiveData;

        receiveData = new byte[myRFTC.UDP_PACKET_SIZE];
        receivePacket = new DatagramPacket(receiveData, myRFTC.UDP_PACKET_SIZE);

        myRFTC.testOut("RcvThread: Waiting for ACKs!");

        while (!isInterrupted()) {
            try {
                // Set SoTimeout due to possible complete loss of Acks
                myRFTC.clientSocket.setSoTimeout(myRFTC.CONNECTION_TIMEOUT);
                // wait for ack
                myRFTC.clientSocket.receive(receivePacket);
            } catch (java.net.SocketTimeoutException e) {
                System.err.println("-------------------------->>>>> Connection Timeout! Server down?");
                return;
            } catch (IOException e) {
                return;
            }

            // Analyse received ACK
            ack = new RFTpacket(receivePacket.getData(), receivePacket.getLength());
            myRFTC.testOut("RcvThread: ACK received for seq num: " + ack.getSeqNum() + " -- sendbase: " + sendbase);

            if (ack.getSeqNum() > sendbase) {
                /* -------- Evaluate ACK ----------- */
                /* ToDo */
                RFTpacket sendbasePacket = myRFTC.sendBuf.getSendbasePacket();

                if (sendbasePacket != null && sendbasePacket.getTimestamp() > 0) {
                    // Calculate sampleRTT in nanoseconds
                    long currentTime = System.nanoTime();
                    long sampleRTT = currentTime - sendbasePacket.getTimestamp();

                    // Update the timeout interval
                    myRFTC.computeTimeoutInterval(sampleRTT);
                }

                // Update sendbase and remove acknowledged packets
                sendbase = ack.getSeqNum();
                myRFTC.sendBuf.remove(sendbase);
                sendbasePacket = myRFTC.sendBuf.getSendbasePacket();
                if (sendbasePacket != null) {
                    myRFTC.rft_timer.startTimer(myRFTC.timeoutInterval, true);
                } else myRFTC.rft_timer.cancelTimer();
                myRFTC.testOut("RcvThread: sendbase update to: " + sendbase);
            } else {
                /* ------- Fast Retransmit ? ----- */
                /* ToDo */
                if (myRFTC.fastRetransmitMode) {
                    if (ack.getSeqNum() == sendbase) {
                        dupCounter++;
                        myRFTC.testOut("RcvThread: Duplicate ACk received for seqnum: " + ack.getSeqNum());

                        if (dupCounter >= 3) {
                            //Trigger fast retransmit
                            myRFTC.timeoutTask();
                            myRFTC.testOut("RcvThread: Triggering fast retransmit for seq num: " + sendbase);

                            // Reset duplicate counter
                            dupCounter = 0;
                        }
                    } else {
                        // Reset dupCounter for non-duplicate ACKs
                        dupCounter = 0;
                    }
                }
            }
        }
    }
}