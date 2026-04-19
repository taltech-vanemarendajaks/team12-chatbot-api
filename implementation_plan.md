# Implement File Upload in Conversation Dropzone

Provide a brief description of the problem, background context, and what the change accomplishes:
You've asked to implement the `// TODO: handle file upload` logic inside the React `ConversationDropzone` component. However, the Bossbot Spring Boot backend currently does not have any endpoints to accept file uploads (e.g., no `MultipartFile` handlers in the controllers), nor does the database schema store file attachments. 

I've outlined two pathways to implement file uploads below. Please review and let me know which approach you prefer.

## User Review Required

> [!IMPORTANT]  
> The backend (`bapi`) currently does not support file attachments. We must decide how a "file upload" should be treated by the chatbot.

**Option A: True File Upload (Attachments)**
This approach treats files as genuine attachments. 
*   **Backend**: We create a new endpoint (e.g., `POST /api/v1/conversations/{id}/files`) that accepts `multipart/form-data` with a `MultipartFile`. We will also need to decide how to store these files (e.g., local disk, or database BLOB) and how the AI will access them.
*   **Frontend**: We submit the file using `FormData` via `axios`.

**Option B: Client-side Text Extraction (Simple)**
If you only intend to support text-based files (like `.txt`, `.csv`, `.md`, or `.json`), we can skip backend file storage entirely.
*   **Frontend**: The React Dropzone reads the content of the file locally using `FileReader`. Then, it sends the file contents as a standard text message to the existing `POST /api/v1/messages` endpoint. 
*   **Backend**: No backend structural changes are required. The AI sees the file contents as a regular chat message.

## Proposed Changes

If we proceed with **Option A (True File Upload)**, here is what will change:

### Backend API (bapi)
*   #### [NEW] `FileController.java`
    *   Add a new `@PostMapping` endpoint for file uploads using `MultipartFile`. 
*   #### [NEW] `FileService.java`
    *   Logic to save the uploaded file to disk.
*   #### [NEW] `FileEntity.java` (Optional)
    *   If we need to track uploaded files in the PostgreSQL database.

### Frontend UI (bui)
*   #### [MODIFY] `chatApi.ts`
    *   Add a `uploadFile(conversationId: number, file: File)` method using `FormData`.
*   #### [MODIFY] `ConversationSidebar.tsx`
    *   Update `onDrop` to fire the new API call and show an upload progress indicator.
    
--- 

If we proceed with **Option B (Text Extraction)**, here is what will change:

### Frontend UI (bui)
*   #### [MODIFY] `ConversationSidebar.tsx`
    *   Update `onDrop` to invoke `FileReader().readAsText(file)`.
    *   Once read, use the existing `postMessage` API to send the file's text content to the backend.
*   #### [MODIFY] `messageApi.ts`
    *   Ensure we have a method hook to correctly POST a new message to the existing API structure.

## Open Questions

> [!WARNING]  
> Before I proceed with writing any code, please tell me:
> 1. Which approach should we take: **Option A** (True attachments with backend updates) or **Option B** (Text extraction on the frontend only)?
> 2. If Option A, where should the backend store the files (local filesystem folder, database, or a cloud provider)?

## Verification Plan
*   **Manual Verification**: 
    1. Drop a valid file onto the sidebar.
    2. Check the browser's Network tab to confirm the data is sent.
    3. (For Option A) Verify the file appears on the backend filesystem. (For Option B) Verify the chatbot responds to the file's contents correctly.
