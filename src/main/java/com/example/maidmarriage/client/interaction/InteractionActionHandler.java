package com.example.maidmarriage.client.interaction;

@FunctionalInterface
public interface InteractionActionHandler {
    void execute(InteractionActionContext context);
}
