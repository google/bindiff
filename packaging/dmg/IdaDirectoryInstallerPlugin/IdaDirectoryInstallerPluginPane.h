#import <Cocoa/Cocoa.h>
#import <InstallerPlugins/InstallerPlugins.h>

@interface IdaDirectoryInstallerPluginPane : InstallerPane {
  IBOutlet NSTextField * uiIdaDirectoryField;
  IBOutlet NSButton * uiBrowseButton;

  // Spotlight query for filling the text field with a default value.
  NSMetadataQuery * query;
}

// Called when the browse button is clicked.
- (IBAction)uiBrowseButtonClicked:(id)sender;

@end
