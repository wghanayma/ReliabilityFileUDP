public interface FileMessageEvent {
    void onMessageReceivedFile(String message);

    void onError(int erorr);
}
