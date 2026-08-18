#include "../include/watcher.h"
#include "../include/fileProcessor.h"
#include <iostream>
#include <sys/inotify.h>
#include <unistd.h>
#include <filesystem>

namespace fs = std::filesystem;

// constants for inofity buffer;
constexpr size_t EVENT_SIZE = sizeof(struct inotify_event);
constexpr size_t BUF_LEN = 1024 * (EVENT_SIZE + 16);

Watcher::Watcher(const std::string& staging_directory, FileProcessor& proc) 
    : stagingDir(staging_directory), processor(proc), running(false), inotify_fd(-1), watch_wd(-1){

        // create staging dir if its not created
        if(fs::exists(stagingDir))
            fs::create_directories(stagingDir);
    }

Watcher::~Watcher() { stop(); }

void Watcher::stop() {
    running = false;

    // cleaning OS' nofity resource
    if(watch_wd >= 0 && inotify_fd >= 0) {
        inotify_rm_watch(inotify_fd, watch_wd);
        watch_wd = -1;
    }

    if (inotify_fd >= 0) {
        close(inotify_fd);
        inotify_fd = -1;
    }

}

void Watcher::start() {
    inotify_fd = inotify_init();
    if (inotify_fd < 0) {
        std::cerr << "[WATCHER] ERROR while initializing inotify system!" << std::endl;
        return;
    }

    // Adding watch on dir 
    // IN_CLOSE_WRITE: Copyng file in directory has ended
    // IN_MOVED_TO: File has moved (cut/paste) in directory.
    watch_wd = inotify_add_watch(inotify_fd, stagingDir.c_str(), IN_CLOSE_WRITE | IN_MOVED_TO);
    if (watch_wd < 0) {
        std::cerr << "[WATCHER] ERROR while adding watch for: " << stagingDir << std::endl;
        close(inotify_fd);
        return;
    }

    std::cout << "[WATCHER] System started. watching directory: " << stagingDir << std::endl;
    running = true;

    char buffer[BUF_LEN];

    // Main loop for daemon
    while (running) {
        // read() je is blocking func, it will wait till somehing has happendend any event in director<
        int length = read(inotify_fd, buffer, BUF_LEN);
        
        if (length < 0) {
            if (running) {
                std::cerr << "[WATCHER] Error while reading inotify event." << std::endl;
            }
            break;
        }

        int i = 0;
        while (i < length) {
            struct inotify_event* event = (struct inotify_event*)&buffer[i];
            
            if (event->len > 0) {
                if ((event->mask & IN_CLOSE_WRITE) || (event->mask & IN_MOVED_TO)) {
                    std::string filename = event->name;
                    
                    // Ignore dot files and temp files
                    if (filename[0] != '.') {
                        fs::path full_path = fs::path(stagingDir) / filename;
                        std::cout << "\n[WATCHER] New file is ready for extraction: " << filename << std::endl;
                        
                        // Foward file to Processor for futher process (parsing -> extraction -> building struct vars -> DB Write)
                        bool success = processor.process(full_path);
                        
                        if (success) {
                            std::cout << "[WATCHER] Succesful indexing: " << filename << std::endl;
                        } else {
                            std::cerr << "[WATCHER] Unsuccesful indexing " << filename << std::endl;
                        }
                    }
                }
            }
            // Move pointer on next event on buffer
            i += EVENT_SIZE + event->len;
        }
    }
}



