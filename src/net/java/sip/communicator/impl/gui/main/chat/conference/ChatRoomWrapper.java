/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.chat.conference;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.customcontrols.*;
import net.java.sip.communicator.impl.gui.main.chat.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.plugin.desktoputil.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * The <tt>ChatRoomWrapper</tt> is the representation of the <tt>ChatRoom</tt>
 * in the GUI. It stores the information for the chat room even when the
 * corresponding protocol provider is not connected.
 *
 * @author Yana Stamcheva
 * @author Damian Minkov
 */
public class ChatRoomWrapper
{
    /**
     * The protocol provider to which the corresponding chat room belongs.
     */
    private final ChatRoomProviderWrapper parentProvider;

    /**
     * The room that is wrapped.
     */
    private ChatRoom chatRoom;

    /**
     * The room name.
     */
    private final String chatRoomName;

    /**
     * The room id.
     */
    private final String chatRoomID;

    /**
     * The property we use to store values in configuration service.
     */
    private static final String AUTOJOIN_PROPERTY_NAME = "autoJoin";

    /**
     * As isAutoJoin can be called from GUI many times we store its value once
     * retrieved to minimize calls to configuration service.
     */
    private Boolean autoJoin = null;

    /**
     * By default all chat rooms are persistent from UI point of view.
     * But we can override this and force not saving it.
     * If not overridden we query the wrapped room.
     */
    private Boolean persistent = null;
    
    /**
     * The prefix needed by the credential storage service to store the password
     * of the chat room.
     */
    private String passwordPrefix;

    /**
     * Creates a <tt>ChatRoomWrapper</tt> by specifying the protocol provider,
     * the identifier and the name of the chat room.
     *
     * @param parentProvider the protocol provider to which the corresponding
     * chat room belongs
     * @param chatRoomID the identifier of the corresponding chat room
     * @param chatRoomName the name of the corresponding chat room
     */
    public ChatRoomWrapper( ChatRoomProviderWrapper parentProvider,
                            String chatRoomID,
                            String chatRoomName)
    {
        this.parentProvider = parentProvider;
        this.chatRoomID = chatRoomID;
        this.chatRoomName = chatRoomName;
        passwordPrefix = ConfigurationUtils.getChatRoomPrefix(
            getParentProvider().getProtocolProvider().getAccountID()
            .getAccountUniqueID(), chatRoomID) + ".password";
        
    }

    /**
     * Creates a <tt>ChatRoomWrapper</tt> by specifying the corresponding chat
     * room.
     *
     * @param parentProvider the protocol provider to which the corresponding
     * chat room belongs
     * @param chatRoom the chat room to which this wrapper corresponds.
     */
    public ChatRoomWrapper( ChatRoomProviderWrapper parentProvider,
                            ChatRoom chatRoom)
    {
        this(   parentProvider,
                chatRoom.getIdentifier(),
                chatRoom.getName());

        this.chatRoom = chatRoom;
    }

    /**
     * Returns the <tt>ChatRoom</tt> that this wrapper represents.
     *
     * @return the <tt>ChatRoom</tt> that this wrapper represents.
     */
    public ChatRoom getChatRoom()
    {
        return chatRoom;
    }

    /**
     * Sets the <tt>ChatRoom</tt> that this wrapper represents.
     *
     * @param chatRoom the chat room
     */
    public void setChatRoom(ChatRoom chatRoom)
    {
        this.chatRoom = chatRoom;
    }

    /**
     * Returns the chat room name.
     *
     * @return the chat room name
     */
    public String getChatRoomName()
    {
        return chatRoomName;
    }

    /**
     * Returns the identifier of the chat room.
     *
     * @return the identifier of the chat room
     */
    public String getChatRoomID()
    {
        return chatRoomID;
    }

    /**
     * Returns the parent protocol provider.
     *
     * @return the parent protocol provider
     */
    public ChatRoomProviderWrapper getParentProvider()
    {
        return this.parentProvider;
    }

    /**
     * Returns <code>true</code> if the chat room is persistent,
     * otherwise - returns <code>false</code>.
     *
     * @return <code>true</code> if the chat room is persistent,
     * otherwise - returns <code>false</code>.
     */
    public boolean isPersistent()
    {
        if(persistent == null)
        {
            if(chatRoom != null)
                persistent = chatRoom.isPersistent();
            else
                return true;
        }

        return persistent;
    }

    /**
     * Change persistence of this room.
     * @param value set persistent state.
     */
    public void setPersistent(boolean value)
    {
        this.persistent = value;
    }
    
    
    /**
     * Stores the password for the chat room.
     * 
     * @param password the password to store
     */
    public void savePassword(String password)
    {
        
        GuiActivator.getCredentialsStorageService()
            .storePassword(passwordPrefix, password);
    }
    
    /**
     * Returns the password for the chat room.
     * 
     * @return the password
     */
    public String loadPassword()
    {
        return GuiActivator.getCredentialsStorageService()
            .loadPassword(passwordPrefix);
    }
    
    /**
     * Removes the saved password for the chat room.
     */
    public void removePassword()
    {
        GuiActivator.getCredentialsStorageService()
            .removePassword(passwordPrefix);
    }

    /**
     * Is room set to auto join on start-up.
     * @return is auto joining enabled.
     */
    public boolean isAutojoin()
    {
        if(autoJoin == null)
        {
            String val = ConfigurationUtils.getChatRoomProperty(
                getParentProvider().getProtocolProvider(),
                getChatRoomID(), AUTOJOIN_PROPERTY_NAME);

            autoJoin = Boolean.valueOf(val);
        }

        return autoJoin;
    }

    /**
     * Changes auto join value in configuration service.
     *
     * @param value change of auto join property.
     */
    public void setAutoJoin(boolean value)
    {
        autoJoin = value;

        // as the user wants to autojoin this room
        // and it maybe already created as non persistent
        // we must set it persistent and store it
        if(!isPersistent())
        {
            setPersistent(true);

            ConfigurationUtils.saveChatRoom(
                getParentProvider().getProtocolProvider(),
                getChatRoomID(),
                getChatRoomID(),
                getChatRoomName());
        }

        if(value)
        {
            ConfigurationUtils.updateChatRoomProperty(
                getParentProvider().getProtocolProvider(),
                chatRoomID, AUTOJOIN_PROPERTY_NAME, Boolean.toString(autoJoin));
        }
        else
        {
            ConfigurationUtils.updateChatRoomProperty(
                getParentProvider().getProtocolProvider(),
                chatRoomID, AUTOJOIN_PROPERTY_NAME, null);
        }
    }

    /**
     * Opens a dialog with a fields for the nickname and the subject of the room
     *  and returns them.
     *
     * @return array with the nickname and subject values.
     */
    public String[] getJoinOptions()
    {
        return getJoinOptions(false);
    }

    /**
     * Opens a dialog with a fields for the nickname and the subject of the room
     *  and returns them.
     *
     * @param dontDisplaySubjectFields if true the subject fields will be hidden
     * @return array with the nickname and subject values.
     */
    public String[] getJoinOptions(boolean dontDisplaySubjectFields)
    {
        String nickName = null;
        ChatRoomJoinOptionsDialog reasonDialog =
            new ChatRoomJoinOptionsDialog(GuiActivator.getResources()
                .getI18NString("service.gui.CHANGE_NICKNAME"), GuiActivator
                .getResources().getI18NString(
                    "service.gui.CHANGE_NICKNAME_LABEL"), false, true, 
                    dontDisplaySubjectFields);
        reasonDialog.setIcon(ImageLoader.getImage(
                    ImageLoader.CHANGE_NICKNAME_ICON));
        
        final OperationSetServerStoredAccountInfo accountInfoOpSet
            =  getParentProvider().getProtocolProvider().getOperationSet(
                    OperationSetServerStoredAccountInfo.class);
        
        String displayName = "";
        if (accountInfoOpSet != null)
        {
            displayName = AccountInfoUtils.getDisplayName(accountInfoOpSet);
        }
        
        if(displayName == null || displayName.length() == 0)
        {
            displayName = GuiActivator.getGlobalDisplayDetailsService()
                .getGlobalDisplayName();
            if(displayName == null || displayName.length() == 0)
            {
                displayName = getParentProvider().getProtocolProvider()
                    .getAccountID().getUserID();
                if(displayName != null)
                {
                    int atIndex = displayName.lastIndexOf("@");
                    if (atIndex > 0)
                        displayName = displayName.substring(0, atIndex);
                }
            }
        }
        reasonDialog.setReasonFieldText(displayName);
        
        int result = reasonDialog.showDialog();

        if (result == MessageDialog.OK_RETURN_CODE)
        {
            nickName = reasonDialog.getReason().trim();
            ConfigurationUtils.updateChatRoomProperty(
                getParentProvider().getProtocolProvider(),
                getChatRoomID(), "userNickName", nickName);
            
        }
        String[] joinOptions = {nickName, reasonDialog.getSubject()};
        return joinOptions;
    }
    
    /**
     * Dialog with fields for nickname and subject.
     */
    private class ChatRoomJoinOptionsDialog extends ChatOperationReasonDialog
    {
        /** 
         * Text field for the subject.
         */
        private SIPCommTextField subject = new SIPCommTextField(GuiActivator
            .getResources().getI18NString("service.gui.SUBJECT"));
        
        /**
         * Label that hides and shows the subject fields panel on click.
         */
        private JLabel cmdExpandSubjectFields;
        
        /**
         * Panel that holds the subject fields.
         */
        private JPanel subjectFieldsPannel = new JPanel(new BorderLayout());
        
        /**
         * Adds the subject fields to dialog. Sets action listeners.
         * 
         * @param title the title of the dialog
         * @param message the message shown in this dialog
         * @param disableOKIfReasonIsEmpty if true the OK button will be 
         * disabled if the reason text is empty.
         * @param showReasonLabel specify if we want the "Reason:" label
         * @param dontDisplaySubjectFields if true the sibject fields will be 
         * hidden.
         */
        public ChatRoomJoinOptionsDialog(String title, String message,
            boolean showReasonLabel,
            boolean disableOKIfReasonIsEmpty,
            boolean dontDisplaySubjectFields)
        {
            super(title,
                message,
                showReasonLabel,
                disableOKIfReasonIsEmpty);
            
            if(dontDisplaySubjectFields)
                return;
            
            JPanel subjectPanel = new JPanel(new BorderLayout());
            subjectPanel.setOpaque(false);
            subjectPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 0, 0, 0));
            
            subjectFieldsPannel.setBorder(
                BorderFactory.createEmptyBorder(10, 30, 0, 0));
            subjectFieldsPannel.setOpaque(false);
            subjectFieldsPannel.add(subject, BorderLayout.CENTER);
            subjectFieldsPannel.setVisible(false);
            subject.setFont(getFont().deriveFont(12f));
            
            cmdExpandSubjectFields = new JLabel();
            cmdExpandSubjectFields.setBorder(new EmptyBorder(0, 5, 0, 0));
            cmdExpandSubjectFields.setIcon(GuiActivator.getResources()
                .getImage("service.gui.icons.RIGHT_ARROW_ICON"));
            cmdExpandSubjectFields.setText(GuiActivator
                .getResources().getI18NString("service.gui.SET_SUBJECT"));
            cmdExpandSubjectFields.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    cmdExpandSubjectFields.setIcon(
                            UtilActivator.getResources().getImage(
                                subjectFieldsPannel.isVisible()
                                        ? "service.gui.icons.RIGHT_ARROW_ICON"
                                        : "service.gui.icons.DOWN_ARROW_ICON"));

                    subjectFieldsPannel.setVisible(
                        !subjectFieldsPannel.isVisible());

                    pack();
                }
            });
            subjectPanel.add(cmdExpandSubjectFields,BorderLayout.NORTH);
            subjectPanel.add(subjectFieldsPannel,BorderLayout.CENTER);
            addToReasonFieldPannel(subjectPanel);
            this.pack();
        }
    
       /**
        * Returns the text entered in the subject field.
        * 
        * @return the text from the subject field.
        */
       public String getSubject()
       {
           return subject.getText();
       }
    }
}
