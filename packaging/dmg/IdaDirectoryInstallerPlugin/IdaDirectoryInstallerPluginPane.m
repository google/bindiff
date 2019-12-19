#import "IdaDirectoryInstallerPluginPane.h"

@implementation IdaDirectoryInstallerPluginPane

- (id)init {
  [super init];

  // Prepare a Spotlight query
  query = [[NSMetadataQuery alloc] init];

  [self performSelectorOnMainThread:@selector(prepareAndStartQuery:)
                         withObject:nil
                      waitUntilDone:NO];

  return self;
}

- (void) prepareAndStartQuery:(void *)obj {
  NSNotificationCenter *nf = [NSNotificationCenter defaultCenter];
  [nf addObserver:self
         selector:@selector(queryDidFinishGatheringNotification:)
             name:NSMetadataQueryDidFinishGatheringNotification
           object:query];

  NSPredicate *idaPred = [NSPredicate predicateWithFormat:
              @"((kMDItemContentType == 'com.apple.application-bundle') && "
              "(kMDItemFSName LIKE 'ida*'))", nil];
  [query setPredicate:idaPred];

  NSSortDescriptor *sortBy =
  [NSSortDescriptor sortDescriptorWithKey:(NSString *)kMDItemFSName
                                ascending:YES];
  [query setSortDescriptors:[NSArray arrayWithObject:sortBy]];

  [query startQuery];
}

- (void) dealloc {
  [[NSNotificationCenter defaultCenter] removeObserver:self];
  [query release];

  [super dealloc];
}

- (NSString *)title {
  NSBundle *bundle = [NSBundle bundleForClass:[self class]];
  return [bundle localizedStringForKey:@"PaneTitle" value:nil table:nil];
}

- (IBAction)uiBrowseButtonClicked:(id)sender {
  NSOpenPanel *panel = [NSOpenPanel openPanel];
  NSWindow *parent = [uiIdaDirectoryField window];

  // Only select application bundles
  [panel setAllowedFileTypes:[NSArray arrayWithObject:@"app"]];
  [panel setCanChooseFiles:NO];
  [panel setCanChooseDirectories:YES];
  [panel setTreatsFilePackagesAsDirectories:YES];
  [panel setDirectoryURL:[NSURL fileURLWithPath:NSHomeDirectory()
                                    isDirectory:YES]];
  [panel beginSheetModalForWindow:parent
                completionHandler:^(NSModalResponse result) {
    if (result != NSModalResponseOK) {
      return;
    }
    [uiIdaDirectoryField setStringValue:
     [NSString stringWithFormat:@"%@", [[panel URL] path]]];
  }];
}

- (void)didEnterPane:(InstallerSectionDirection)dir {
  if (dir != InstallerDirectionForward) {
    return;
  }
}

- (BOOL)shouldExitPane:(InstallerSectionDirection)dir {
  if (dir == InstallerDirectionBackward) {
    return YES;
  }

  NSString *idaDir = [uiIdaDirectoryField stringValue];
  NSWindow *parent = [uiIdaDirectoryField window];

  // If we were given an application bundle name that ends with ".app", deal
  // with the additional two directory layers in a MacOS package. Otherwise,
  // assume that the user entered a full path themselves.
  NSString *idaPluginDir =
      [NSString stringWithFormat:@"%@%@/plugins", idaDir,
       [idaDir hasSuffix:@".app"] ? @"/Contents/MacOS" : @""];

  NSFileManager *fm = [[NSFileManager alloc] init];
  BOOL isDir;
  BOOL haveValidDir = ([fm fileExistsAtPath:idaPluginDir
                                isDirectory:&isDir] && isDir);
  if (!haveValidDir && ([idaDir length] > 0)) {
    NSAlert *alert = [[NSAlert alloc] init];
    [alert setMessageText:@"Invalid IDA installation directory"];
    [alert setInformativeText:@"Please enter the full path to a valid "
                               "installation of Hex-Rays IDA Pro."];
    [alert addButtonWithTitle:@"OK"];
    [alert beginSheetModalForWindow:parent completionHandler:nil];
    [alert release];
  }

  // Indicate to postinstall script that no IDA Pro directory was selected.
  if ([idaDir length] == 0) {
    idaPluginDir = @"IDADIR/plugins";
  }

  // Write the IDA installation path to a known file in /tmp that will be
  // used and later cleaned up by the postinstall script. We do not use the
  // TMPDIR environment variable because the Installer puts scripts inside
  // a sandbox.
  NSString *const kKnownFile =
      @"/tmp/__38F74084-9DF1-4C5D-91E1-0E63780ADC57_zy__";

  // Remove file first
  [fm removeItemAtPath:kKnownFile error:nil];

  // Write new file atomically
  [idaPluginDir writeToFile:kKnownFile
                 atomically:YES
                   encoding:NSUTF8StringEncoding
                      error:nil];

  [fm release];
  return haveValidDir || ([idaDir length] == 0);
}

- (void)queryDidFinishGatheringNotification:(NSNotification *)note {
  if ([query resultCount] == 0) {
    return;
  }

  NSMetadataItem *item = [query resultAtIndex:0];
  NSString *path = [[item valueForAttribute:(NSString *)kMDItemPath]
                    stringByResolvingSymlinksInPath];
  [uiIdaDirectoryField setStringValue:path];
}

@end
