import SwiftUI

@main
struct iOSApp: App {
    // Bridges UIKit lifecycle callbacks to SwiftUI so we can register for
    // remote notifications (APNs) at launch. See AppDelegate.swift.
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}