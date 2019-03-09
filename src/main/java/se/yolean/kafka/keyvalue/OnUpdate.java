package se.yolean.kafka.keyvalue;

public interface OnUpdate {

  /**
   *
   * Transitional strategy for handling downstream errors:
   * - Keep retrying
   * + don't return success from any subsequent {@link #handle(UpdateRecord, Completion)}
   * + when bailing throw on the next handle
   * = should lead to service restart without commits from the failed offset.
   *
   * @param update The new value (which may be the old value at a new offset)
   * @param completion Handles outcome of the update
   */
  void handle(UpdateRecord update, Completion completion);

  public interface Completion {

    /**
     * Called if the hooks succeed (after retries, if applicable)
     */
    void onSuccess();

    /**
     * Calledf any of the hooks fail (after retries, if applicable)
     */
    void onFailure();

  }

}
