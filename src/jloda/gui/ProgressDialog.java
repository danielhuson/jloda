/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package jloda.gui;

import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgramProperties;
import jloda.util.ProgressListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Stack;

/**
 * A progress bar dialog that updates via the swing event queue
 *
 * @author huson
 *         Date: 02-Dec-2003
 */
public class ProgressDialog implements ProgressListener {
    static private long delayInMilliseconds = 2000;// wait two seconds before opening progress bar
    private final long startTime = System.currentTimeMillis();
    private JDialog dialog;
    private boolean closed = false;
    private boolean visible = false;
    private JProgressBar progressBar;
    boolean userCancelled;
    private JLabel taskLabel = new JLabel();
    private JButton cancelButton;
    private boolean closeOnCancel = true;
    private String task;
    private String subtask;
    private boolean debug = false;

    private boolean shiftedDown = false;

    private long maxProgess = 0;
    private long currentProgress = 0;

    private StatusBar frameStatusBar = null;
    private JPanel statusBarPanel = null;

    private boolean cancelable = true;

    /**
     * Constructs a Progress Dialog with a given task name and subtask name. The dialog is embedded into
     * the given frame. If frame = null then the dialog will appear as a separate window.
     *
     * @param taskName
     * @param subtaskName
     * @param owner
     */
    public ProgressDialog(final String taskName, final String subtaskName, final Component owner) {
        MakeProgressDialog(taskName, subtaskName, owner, delayInMilliseconds);
        checkTimeAndShow();
        if (dialog != null)
            dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public ProgressDialog(final String taskName, final String subtaskName, final Component owner, final long delayInMillisec) {
        MakeProgressDialog(taskName, subtaskName, owner, delayInMillisec);
        checkTimeAndShow();
        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /**
     * Constructs a Progress Dialog with a given task name and subtask name. The dialog is embedded into
     * the given frame. If frame = null then the dialog will appear as a separate window.
     *
     * @param taskName
     * @param subtaskName
     * @param owner
     */
    public void MakeProgressDialog(final String taskName, final String subtaskName, final Component owner, final long delayInMillisec) {
//final String note = Basic.toMessageString(note0);

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    userCancelled = false;
                    delayInMilliseconds = delayInMillisec;
                    Frame parent = null;
                    if (owner instanceof JFrame)
                        parent = (Frame) owner;
                    dialog = new JDialog(parent, "Progress...");

                    dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    // dialog.setResizable(false);

// the label:
                    taskLabel = new JLabel();
                    task = taskName;
                    subtask = subtaskName;
                    updateTaskLabel();

// the progress bar:
                    progressBar = new JProgressBar(0, 150);
                    progressBar.setValue(-1);
                    progressBar.setIndeterminate(true);
                    progressBar.setStringPainted(false);
                    if (ProgramProperties.isMacOS()) { //On the mac - make like the standard p bar
                        Dimension d = progressBar.getPreferredSize();
                        d.height = 10;
                        progressBar.setPreferredSize(d);
                        d = progressBar.getMaximumSize();
                        d.height = 10;
                        progressBar.setMaximumSize(d);
                    }

// the cancel button:
                    cancelButton = new JButton();
                    resetCancelButtonText();
                    cancelButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            try {
                                setUserCancelled(true);
                                checkForCancel();
                            } catch (CanceledException e1) {
                            }
                        }
                    });

                    if (!isCancelable())
                        cancelButton.setEnabled(false);

                        frameStatusBar = findStatusBar(owner);

                        if (frameStatusBar != null) {
                            statusBarPanel = new JPanel();
                            statusBarPanel.setLayout(new BorderLayout());
                            progressBar.setPreferredSize(new Dimension(300, 10));
                            statusBarPanel.add(progressBar, BorderLayout.CENTER);
                            cancelButton.setPreferredSize(new Dimension(60, 14));
                            cancelButton.setMinimumSize(new Dimension(60, 14));
                            cancelButton.setFont(new Font("Dialog", Font.PLAIN, 12));
                            cancelButton.setBorder(BorderFactory.createEtchedBorder());
                            statusBarPanel.add(cancelButton, BorderLayout.EAST);
                            return; // done
                        }

                    if (!ProgramProperties.isMacOS()) { // none mac progress dialog:
                        GridBagLayout gridBag = new GridBagLayout();
                        JPanel pane = new JPanel(gridBag);
                        pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                        GridBagConstraints c = new GridBagConstraints();

                        c.anchor = GridBagConstraints.CENTER;

                        c.fill = GridBagConstraints.HORIZONTAL;
                        c.weightx = 3;
                        c.weighty = 1;
                        c.gridx = 1;
                        c.gridy = 0;
                        c.gridwidth = 3;
                        c.gridheight = 1;

                        pane.add(taskLabel, c);

                        c.anchor = GridBagConstraints.CENTER;
                        c.fill = GridBagConstraints.NONE;
                        c.weightx = 1;
                        c.weighty = 5;
                        c.gridx = 1;
                        c.gridy = 1;
                        c.gridwidth = 3;
                        c.gridheight = 1;

                        pane.add(progressBar, c);

                        c.anchor = GridBagConstraints.CENTER;
                        c.weightx = 1;
                        c.weighty = 1;
                        c.gridx = 1;
                        c.gridy = 2;
                        c.gridwidth = 1;
                        c.gridheight = 1;

                        pane.add(cancelButton, c);

                        dialog.getContentPane().add(pane);
                        dialog.setSize(new Dimension(550, 120));
                    } else {  // mac os progress dialog:
                        JPanel contentPane = new JPanel(new BorderLayout());
                        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//Progress Bar and cancel button.
                        JPanel barpane = new JPanel();
                        barpane.setLayout(new BoxLayout(barpane, BoxLayout.LINE_AXIS));
                        barpane.add(progressBar);

                        barpane.add(cancelButton);

                        JPanel taskPanel = new JPanel();
                        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.PAGE_AXIS));
                        taskPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
                        taskPanel.add(taskLabel);
                        taskPanel.add(Box.createHorizontalGlue());

                        //Put everything into the content pane
                        contentPane.add(barpane, BorderLayout.PAGE_START);
                        contentPane.add(taskPanel, BorderLayout.LINE_START);
                        dialog.setContentPane(contentPane);
                        dialog.setSize(new Dimension(550, 120));
                    }

                    if (parent != null) {
                        int x = parent.getX();
                        int y = parent.getY();
                        int dx = parent.getWidth() - dialog.getWidth();
                        int dy = parent.getHeight() - dialog.getHeight();
                        x += dx / 2;
                        y += dy / 2;

                        dialog.setLocation(x, y);
                    }
                    //dialog.setVisible(true);  //open once delay has passed
                }
            });
        } catch (Exception ex) {
            // Basic.caught(ex);
        }
    }

    /**
     * determine whether given component contains a statusbar
     *
     * @param component
     * @return statusbar or null
     */
    private static StatusBar findStatusBar(Component component) {
        if (component instanceof Container) {
            Container frame = (Container) component;
            Stack<Component> stack = new Stack<>();
            stack.addAll(Arrays.asList(frame.getComponents()));
            while (stack.size() > 0) {
                Component c = stack.pop();
                if (c instanceof StatusBar)
                    return (StatusBar) c;
                else if (c instanceof Container)
                    stack.addAll(Arrays.asList(((Container) c).getComponents()));
            }
        }
        return null;
    }


    /**
     * sets the steps number of steps to be done. By default, the maximum is set to 100
     *
     * @param steps0
     */
    public void setMaximum(final long steps0) {
        final int steps;
        if (steps0 > 10000000) {
            steps = (int) (steps0 >> 8l);
            shiftedDown = true;
        } else {
            steps = (int) steps0;
            shiftedDown = false;
        }
        maxProgess = steps;
        checkTimeAndShow();

        if (progressBar != null && maxProgess != progressBar.getMaximum()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        // progressBar.setValue(0);
                        progressBar.setMaximum(steps);
                    }
                });
            } catch (InterruptedException | InvocationTargetException e) {
                Basic.caught(e);
            }
        }
    }

    /**
     * sets the progress. If a negative value is given, sets the progress bar to indeterminate mode
     *
     * @param steps0
     */
    public void setProgress(long steps0) throws CanceledException {
        final int steps;
        if (shiftedDown) {
            steps = (int) (steps0 >> 8l);
        } else {
            steps = (int) steps0;
        }
        currentProgress = steps;
        checkForCancel();

        if (progressBar != null && currentProgress != progressBar.getValue()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (currentProgress < 0) {
                        progressBar.setIndeterminate(true);
                        progressBar.setString(null);
                    } else {
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(steps);
                    }
                }
            });
        }
    }

    /**
     * gets the current progress
     *
     * @return progress
     */
    public long getProgress() {
        if (shiftedDown)
            return progressBar.getValue() << 8;
        else
            return progressBar.getValue();
    }

    /**
     * increment the progress
     *
     * @throws CanceledException
     */
    public void incrementProgress() throws CanceledException {
        currentProgress++;
        checkForCancel();

        if (progressBar != null && currentProgress != progressBar.getValue()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setValue((int) currentProgress);
                }
            });
        }
    }


    /**
     * closes the dialog.
     */
    public void close() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (!closed) {
                    if (statusBarPanel != null) {
                        frameStatusBar.setExternalPanel1(null, false);
                        frameStatusBar.setComponent2(statusBarPanel, false);
                        statusBarPanel = null;
                    }
                    if (dialog != null) {
                        dialog.setVisible(false);
                        dialog.dispose();
                        dialog = null;
                    }
                    closed = true;
                    visible = false;
                }
            }
        });
    }

    /**
     * has user canceled?
     *
     * @throws CanceledException
     */
    public void checkForCancel() throws CanceledException {
        checkTimeAndShow();

        if (this.userCancelled) {
            //dialog.setVisible(false);
            if (closeOnCancel)
                close();

            throw new CanceledException();
        }
    }


    /**
     * sets the subtask name
     *
     * @param subtaskName
     * @throws CanceledException
     */
    public void setSubtask(String subtaskName) {
        checkTimeAndShow();

        if ((subtaskName == null && subtask != null) || (subtaskName != null && (subtask == null || !subtask.equals(subtaskName)))) {
            subtask = subtaskName;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateTaskLabel();
                }
            });
        }
    }


    /**
     * Sets the task name (first description, printed in bold)  and subtask
     *
     * @param taskName
     * @param subtaskName
     * @throws CanceledException
     */
    public void setTasks(String taskName, String subtaskName) {
        checkTimeAndShow();

        if ((taskName == null && task != null) || (taskName != null && (task == null || !task.equals(taskName)))
                || (subtaskName == null && subtask != null) || (subtaskName != null && (subtask == null || !subtask.equals(subtaskName)))) {
            task = taskName;
            subtask = subtaskName;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateTaskLabel();
                }
            });
        }
    }

    private void updateTaskLabel() {
        String label = "<html><p style=\"font-size:" + (statusBarPanel != null ? "10pt" : "12pt") + ";\">";
        if (this.task != null)
            label += "<b>" + this.task + "</b>";
        if (this.task != null && this.subtask != null)
            label += ": ";
        if (this.subtask != null)
            label += this.subtask;
        label += "</font></p>";
        if (statusBarPanel != null) {
            frameStatusBar.setExternalPanel1(new JLabel(label), true);
            statusBarPanel.setToolTipText(label);
        } else
            taskLabel.setText(label);
    }

    public boolean isUserCancelled() {
        return userCancelled;
    }

    public void setUserCancelled(boolean userCancelled) {
        this.userCancelled = userCancelled;
    }

    private void checkTimeAndShow() {
        try {
            if (!closed && !visible && System.currentTimeMillis() - startTime > delayInMilliseconds) {
                show();
            }
        } catch (Exception ex) {
        }
    }

    public void show() {
        if (!visible) {
            try {
                Runnable runnable = new Runnable() {
                    public void run() {
                        if (progressBar != null) {
                            progressBar.setMaximum((int) maxProgess);
                            if (currentProgress < 0) {
                                progressBar.setIndeterminate(true);
                                progressBar.setString(null);
                            } else {
                                progressBar.setIndeterminate(false);
                                progressBar.setValue((int) currentProgress);
                            }
                        }
                        if (statusBarPanel != null)
                            frameStatusBar.setComponent2(statusBarPanel, !closed);
                        else if (dialog != null)
                            dialog.setVisible(true);
                        visible = true;
                    }
                };
                if (!SwingUtilities.isEventDispatchThread()) {
                    SwingUtilities.invokeAndWait(runnable);
                } else
                    runnable.run();
            } catch (InterruptedException e) {
            } catch (InvocationTargetException e) {
                Basic.caught(e);
            }
        }
    }

    public static long getDelayInMilliseconds() {
        return delayInMilliseconds;
    }

    public static void setDelayInMilliseconds(long delayInMilliseconds) {
        ProgressDialog.delayInMilliseconds = delayInMilliseconds;
    }


    /**
     * in debug mode, report tasks and subtasks to stderr, too
     *
     * @return verbose mode
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * in debug mode, report tasks and subtasks to stderr, too
     *
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * is user allowed to cancel?
     *
     * @param cancelable
     */
    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        if (cancelButton != null)
            cancelButton.setEnabled(cancelable);
    }

    /**
     * is user allowed to cancel
     *
     * @return cancelable?
     */
    public boolean isCancelable() {
        return cancelable;
    }

    public void setCancelButtonText(String text) {
        cancelButton.setText(text);
    }

    public void resetCancelButtonText() {
        if (ProgramProperties.isMacOS())
            cancelButton.setText("Stop");
        else
            cancelButton.setText("Cancel");

    }

    /**
     * gets the window that owns this dialog
     *
     * @return owner
     */
    public Window getOwner() {
        return dialog.getOwner();
    }

    public boolean isCloseOnCancel() {
        return closeOnCancel;
    }

    public void setCloseOnCancel(boolean closeOnCancel) {
        this.closeOnCancel = closeOnCancel;
    }
}
