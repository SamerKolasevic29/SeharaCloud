#include <iostream>
#include <csignal>
#include <cstdlib>
#include <filesystem>
#include "../include/db.h"
#include "../include/fileProcessor.h"
#include "../include/watcher.h"

namespace fs = std::filesystem;

// Global pointer on watcher for stooping via singal handler
Watcher* global_watcher = nullptr;

// Func for turning off daemon
void handle_signal(int signal) {
    std::cout << "\n[DAEMON] Recieved signal (" << signal << ") for turning off. Stopping watcher..." << std::endl;
    if (global_watcher != nullptr) {
        global_watcher->stop();
    }
}

int main() {
    // Registrations of OS signals (SIGINT = Ctrl+C, SIGTERM = systemctl stop / docker stop)
    std::signal(SIGINT, handle_signal);
    std::signal(SIGTERM, handle_signal);

    std::cout << "==================================================" << std::endl;
    std::cout << "  SeharaCloud Indexer Daemon Started" << std::endl;
    std::cout << "==================================================" << std::endl;

    // 1. Loading con string from Environment vars
    const char* env_conn_str = std::getenv("DB_CONNECTION_STRING");
    std::string conn_str = env_conn_str ? env_conn_str : "postgresql://user:password@localhost:5432/seharacloud";

    // 2. Paths (We can allow oerriding via ENV vars)
    const char* env_phys_base = std::getenv("PHYSICAL_BASE_PATH");
    std::string physical_base = env_phys_base ? env_phys_base : "/data/seharacloud/media";

    const char* env_log_base = std::getenv("LOGICAL_BASE_PATH");
    std::string logical_base = env_log_base ? env_log_base : "/mnt/cloud";

    std::string staging_dir = physical_base + "/staging";

    try {
        // 3. Inicialization of DB and FileProcessor
        Database db(conn_str);
        FileProcessor processor(db, physical_base, logical_base);

        // 4. Initial scan (Catch-up)
        // If theres some files brigned in when daemon was off
        std::cout << "[DAEMON] Checking remaining files in: " << staging_dir << std::endl;
        if (fs::exists(staging_dir)) {
            for (const auto& entry : fs::directory_iterator(staging_dir)) {
                if (entry.is_regular_file()) {
                    std::string filename = entry.path().filename().string();
                    if (filename[0] != '.') { // Ignoring dot files
                        std::cout << "[DAEMON] Found remaining file: " << filename << std::endl;
                        processor.process(entry.path());
                    }
                }
            }
        }

        // 5. Starting Inotify watcher
        Watcher watcher(staging_dir, processor);
        global_watcher = &watcher;

        // watcher.start() blocks main thread and goes to endless loop unles calling stop()
        watcher.start();

    } catch (const std::exception& e) {
        std::cerr << "[DAEMON] critical error: " << e.what() << std::endl;
        return EXIT_FAILURE;
    }

    std::cout << "[DAEMON] Process succesfuly stupped!" << std::endl;
    return EXIT_SUCCESS;
}