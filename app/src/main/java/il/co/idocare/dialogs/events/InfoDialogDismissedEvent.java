package il.co.idocare.dialogs.events;

/**
 * This event will be posted to EventBus when InfoDialog is dismissed
 */
public class InfoDialogDismissedEvent extends BaseDialogEvent {

    public InfoDialogDismissedEvent(String tag) {
        super(tag);
    }
}
