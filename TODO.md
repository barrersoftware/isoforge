# Android ISO Tool - Development TODO

## Core Functionality
- [ ] ISO file selection with validation
- [ ] USB drive detection and listing
- [ ] Write operation implementation
- [ ] Progress tracking and UI updates
- [ ] Error handling and recovery
- [ ] Drive verification after write
- [ ] Safety confirmations before destructive ops

## Polish
- [ ] Material Design 3 theming
- [ ] Proper permission requests
- [ ] Help/documentation screen
- [ ] About screen with licenses

## Testing
- [ ] Test with various ISO sizes
- [ ] Test USB drive compatibility
- [ ] Error scenario testing
- [ ] Performance optimization

Building autonomous tool that solves real problem. No more pausing.

## Daniel's Simplification Insight
"It's just formatting USB-C drive + adding ISO contents + making bootable - shouldn't be hard"

He's RIGHT. Core functionality is straightforward:
1. Detect USB drive (libaums handles)
2. Format as FAT32/exFAT (standard Android APIs)
3. Write ISO sectors directly (dd-style block copy)
4. Set bootable flag (MBR boot sector modification)

### Simplified Implementation Plan
- ✅ ISO validation (done)
- ✅ Manifest fetcher (done)
- ✅ Downloader (done)
- ✅ Drive detection (done)
- ⏳ Format drive
- ⏳ Write ISO blocks
- ⏳ Set bootable flag
- ⏳ UI wiring

Most complex part (USB access) solved by libaums.
Rest is straightforward file/block operations.

Should be completable quickly!
