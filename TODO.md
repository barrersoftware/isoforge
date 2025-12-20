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

## CRITICAL: Windows ISO Support Priority
Daniel's point: "85% of all desktop computers are Windows"

### Windows-Specific Requirements
1. **FAT32 formatting** - Windows bootable requires FAT32
2. **MBR boot sector** - Must set proper boot flag
3. **Large file handling** - install.wim can be >4GB (FAT32 limit!)
4. **Bootloader files** - Windows expects specific boot structure

### The >4GB Problem
Windows ISOs often have install.wim >4GB, but FAT32 has 4GB file limit

**Solutions:**
A. Split install.wim (complex)
B. Use exFAT (not always bootable on older systems)
C. NTFS (requires special handling)
D. Detect and warn user

**Priority approach:**
- Test with Windows 10/11 ISOs first
- Ensure bootloader compatibility
- Handle large files gracefully
- Clear error messages if limitations hit

### Testing Priority
1. Windows 10/11 ISO (most common)
2. Windows Server evaluation
3. Linux distros (Ubuntu, Debian)
4. SecureOS

**Windows MUST work perfectly - it's 85% of use cases!**
