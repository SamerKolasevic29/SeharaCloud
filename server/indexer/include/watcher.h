#ifndef WATCHER_H
#define WATCHER_H

#include <string>
// first time using atomic
#include <atomic>
#include "fileProcessor.h"

class Watcher {
private:    
    std::string stagingDir;
    FileProcessor& processor;
    std::atomic<bool> running;  //thread-safe flag for turning off daemon
    int inotify_fd;
    int watch_wd;

public:
    Watcher(const std::string& staging_directory, FileProcessor& proc);
    ~Watcher();

    // starting endless loop for looking events on directory
    void start();

    // Stops looking for events (this will turn off daemon when calling from main)
    void stop();

};





#endif
