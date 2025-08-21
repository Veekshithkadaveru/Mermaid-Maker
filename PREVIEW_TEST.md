# Live Preview System - Testing Guide

## Implementation Summary

✅ **Completed Features:**
1. **Mermaid.js Integration**
   - Downloaded Mermaid.js v10.6.1 to `app/src/main/assets/mermaid.min.js`
   - Created `mermaid_preview.html` with full Mermaid configuration
   - Offline-capable preview system

2. **Real-time Preview Rendering** 
   - `MermaidPreview` composable with WebView integration
   - JavaScript bridge for Android ↔ WebView communication
   - Debounced rendering (150ms) for smooth performance
   - Error handling and loading states

3. **Preview Synchronization with Editor**
   - Integrated into `CreateDiagramScreen` with split-view layout
   - Real-time sync when `editorContent` changes
   - Template changes trigger immediate preview updates

## Testing Instructions

### 1. Basic Functionality Test
1. Open the app and navigate to **Create** tab
2. You should see split view: Editor (left) + Preview (right)
3. Type basic Mermaid syntax:
   ```
   graph TD
       A[Start] --> B[Process]
       B --> C[End]
   ```
4. **Expected:** Preview renders diagram within ~200ms

### 2. Real-time Sync Test
1. In the editor, modify the diagram:
   ```
   graph TD
       A[Start] --> B{Decision?}
       B -->|Yes| C[Process A]
       B -->|No| D[Process B]
       C --> E[End]
       D --> E
   ```
2. **Expected:** Preview updates as you type (debounced)

### 3. Template Test
1. Select different diagram types (Flowchart, Sequence, etc.)
2. Choose different templates from the quick templates
3. **Expected:** Preview immediately shows the selected template

### 4. Error Handling Test
1. Type invalid syntax: `invalid mermaid syntax`
2. **Expected:** Error message appears in preview without crashing
3. Fix syntax - preview should recover

### 5. Advanced Test (Settings → Test Preview System)
1. Navigate to **Settings** tab
2. Tap "Test Preview System" 
3. Use test buttons: Flowchart, Sequence, Error
4. **Expected:** Each button shows different diagram/error states

## Debugging

### Check Logs
```bash
adb logcat | grep -E "(MermaidPreview|WebView|Console)"
```

### Key Log Messages to Look For
- ✅ `"Mermaid Preview HTML loaded"`
- ✅ `"Mermaid initialized with version: X.X.X"`
- ✅ `"WebView ready callback received"`
- ✅ `"Render success: XXXX characters"`
- ❌ `"Render error: ..."`

### Common Issues & Solutions

1. **Preview shows "Initializing..." forever**
   - Check if `mermaid.min.js` is in assets folder
   - Check WebView console logs for JavaScript errors

2. **No preview updates when typing**
   - Verify `editorContent` state is updating in `CreateDiagramScreen`
   - Check if `MermaidPreview` `LaunchedEffect` is triggering

3. **Blank preview with valid syntax**
   - Check WebView JavaScript permissions
   - Verify file:// URL access permissions

4. **App crashes on preview**
   - Check for missing dependencies in `build.gradle.kts`
   - Verify WebView is properly configured

## Performance Expectations

- **Render Time:** < 200ms for simple diagrams
- **Memory Usage:** WebView adds ~20-30MB
- **Debounce Delay:** 150ms (adjustable in code)
- **Supported Diagrams:** All Mermaid.js types (flowchart, sequence, class, state, etc.)

## Next Steps

After verifying the preview system works:
1. **SVG Export:** Use `getRenderedSVG()` function from preview
2. **Theme Support:** Implement dark/light theme switching
3. **Performance:** Add "Pause Preview" toggle for large diagrams
4. **Mobile Optimization:** Responsive layout for portrait/landscape

## File Structure Created

```
app/src/main/
├── assets/
│   ├── mermaid.min.js (2.8MB)
│   └── mermaid_preview.html
└── java/.../ui/
    └── preview/
        └── MermaidPreview.kt
```

