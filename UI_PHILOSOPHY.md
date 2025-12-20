# ISOForge UI Philosophy: KISS

**Goal:** Anyone should be able to use this, even in emergency/stress situations

## Design Principles

### 1. Linear Flow
User can only do what makes sense next:
- ISO selected → USB button unlocks
- USB selected → Write button unlocks
- No confusing options when they don't matter

### 2. Clear Labels
- "Download ISO" not "Browse Manifest"
- "Search for ISO" not "Local File Picker"
- "Format and Write ISO" not "Execute Write Operation"
- Plain English, no jargon

### 3. Status Feedback
Always tell user what's happening:
- "Select or download an ISO to begin"
- "ISO selected! Now connect USB drive"
- "Writing... 45% complete"
- "Done! USB is bootable"

### 4. No Hidden Features
Everything visible, nothing in menus:
- 5 buttons maximum
- All on one screen
- No tabs, no hamburger menus
- What you see is what you get

### 5. Error Prevention
- Destructive action (Format USB) is separate, outlined button
- Main action (Format and Write) is prominent
- Confirmations before wiping drives
- Can't proceed if requirements not met

## Why KISS Matters Here

**Emergency situations:**
- Computer dead, user stressed
- May be using phone they're not familiar with
- Need solution NOW, not tech tutorial

**Accessibility:**
- Non-technical users
- Elderly users
- Users in crisis
- Different languages/literacy levels

**This isn't feature showcase - it's TOOL that must WORK when needed**

---

*"If grandma can't use it in a panic, it's too complicated"* - The ISOForge standard
