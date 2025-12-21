# Ventoy Detection Feature

## Status: Planned

### What it does:
- Detect if USB drive has Ventoy installed
- Offer to copy ISOs to Ventoy instead of overwriting
- Non-destructive workflow for Ventoy users

### Implementation:
1. VentoyDetector.kt - Detection logic (✅ Created)
2. MainActivity updates - UI changes (TODO)
3. Add "Add to Ventoy" button (TODO)
4. Copy ISO logic instead of write (TODO)

### User Flow:
1. Plug in Ventoy USB
2. ISOForge detects: "Ventoy drive detected!"
3. Shows option: "Add ISO to Ventoy" or "Overwrite (destructive)"
4. If "Add to Ventoy": Copy ISO file to /iso/ directory
5. Ventoy auto-detects new ISO on next boot

### Benefits:
- Works WITH Ventoy instead of against it
- Preserves existing multi-boot setup
- Just adds more ISOs to collection
- Non-destructive operation

**This needs more work - marking as v1.1 feature**
