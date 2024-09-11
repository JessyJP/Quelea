class GenericListbox {
    constructor(root, onSelectionChange, updateCountCallback = null, getItemCallback = null) {
        this.root = root;
        this.itemMap = {};  // Internal map of item indices to unique identifiers
        this.getItemCallback = getItemCallback;  // Optional callback to fetch items
        this.onSelectionChange = onSelectionChange;  // Callback for when the selection changes
        this.updateCountCallback = updateCountCallback;  // Callback to update the count of items
        this.container = this.createContainer(root);  // Create the listbox container
        this.bindEvents();
    }

    // Create the listbox container and append it to the parent element
    createContainer(parent) {
        const listFrame = document.createElement('div');
        listFrame.classList.add('generic-container-frame');
        parent.appendChild(listFrame);

        const container = document.createElement('ul');  // Unordered list to represent the listbox
        container.classList.add('generic-listbox');
        listFrame.appendChild(container);

        console.info('Generic listbox container created.');
        return container;
    }

    // Populate the listbox with a set of items
    populate(items) {
        console.debug(`Populating listbox with ${items.length} items.`);
        this.container.innerHTML = ''; // Clear current items
        this.itemMap = {};

        this.itemMap = items.reduce((map, item, idx) => {
            const listItem = document.createElement('li');
            listItem.textContent = item.label;  // Set the text content of the list item
            this.container.appendChild(listItem);
            this.itemMap[idx] = item.id;  // Map the index to a unique identifier
            map[item.id] = item;  // Create a reverse map for quick lookup by ID
            return map;
        }, {});

        this.updateCount(items.length);  // Update the count of items
    }

    // Get the currently selected item
    getSelection() {
        const selectedItems = this.container.querySelectorAll('li.selected');
        if (selectedItems.length > 0) {
            const index = Array.from(this.container.children).indexOf(selectedItems[0]);
            const itemId = this.itemMap[index];
            if (itemId) {
                return this.itemMap[itemId] || (this.getItemCallback && this.getItemCallback(itemId));
            }
        }
        return null;
    }

    // Set the selection to a specific item
    setSelection(selection) {
        if (selection) {
            const index = Object.values(this.itemMap).indexOf(selection.id);
            const listItems = this.container.children;
            listItems[index].classList.add('selected');  // Mark as selected
            listItems[index].scrollIntoView({ behavior: 'smooth' });  // Scroll into view
            console.debug(`Set selection to item: ${selection.id}`);
        }
    }

    // Navigate through the items (up/down)
    navigate(direction) {
        const selectedItem = this.container.querySelector('li.selected');
        if (selectedItem) {
            const index = Array.from(this.container.children).indexOf(selectedItem);
            selectedItem.classList.remove('selected');

            // Navigate up
            if (direction === 'up' && index > 0) {
                this.container.children[index - 1].classList.add('selected');
                this.container.children[index - 1].scrollIntoView({ behavior: 'smooth' });
            }

            // Navigate down
            else if (direction === 'down' && index < this.container.children.length - 1) {
                this.container.children[index + 1].classList.add('selected');
                this.container.children[index + 1].scrollIntoView({ behavior: 'smooth' });
            }

            // If at bounds, reselect the current item
            else {
                selectedItem.classList.add('selected');
            }

            this.triggerSelectionChange();
        }
    }

    // Bind events like click and key navigation
    bindEvents() {
        this.container.addEventListener('click', (e) => {
            const clickedItem = e.target.closest('li');
            if (clickedItem) {
                Array.from(this.container.children).forEach(li => li.classList.remove('selected'));  // Clear previous selections
                clickedItem.classList.add('selected');  // Mark the clicked item as selected
                this.triggerSelectionChange();
            }
        });

        this.container.addEventListener('dblclick', () => this.onDoubleClick());

        // Handle keyboard navigation (up/down)
        document.addEventListener('keydown', (e) => {
            if (e.key === 'w') this.navigate('up');
            if (e.key === 's') this.navigate('down');
        });
    }

    // Trigger the selection change callback
    triggerSelectionChange() {
        const selectedElement = this.getSelection();
        if (this.onSelectionChange) {
            this.onSelectionChange(selectedElement);
        }
    }

    // Handle double click events
    onDoubleClick() {
        console.info('Double-click event triggered in GenericListbox.');
    }

    // Update the count of items and call the count callback
    updateCount(count) {
        if (this.updateCountCallback) {
            this.updateCountCallback(count);
        }
        console.debug(`Updated item count: ${count}`);
    }
}

// Inject the CSS directly into the document
const style = document.createElement('style');
style.textContent = `
    /* Generic Listbox CSS */
    .generic-container-frame {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        box-sizing: border-box;
    }

    .generic-listbox {
        list-style-type: none;
        padding: 0;
        margin: 0;
        width: 100%;
        height: 400px;
        overflow-y: auto;
        border: 1px solid #ccc;
        font-family: Arial, sans-serif;
        font-size: 14px;
        box-sizing: border-box;
    }

    .generic-listbox li {
        height: 38px;
        padding: 1px 8px;
        border-bottom: 1px solid #eee;
        cursor: pointer;
        transition: background-color 0.3s ease;
    }

    .generic-listbox li.selected {
        background-color: #4CAF50;
        color: white;
    }

    .generic-listbox li:hover {
        background-color: #f1f1f1;
    }

    /* Custom scrollbar for listbox */
    .generic-listbox::-webkit-scrollbar {
        width: 8px;
    }
    .generic-listbox::-webkit-scrollbar-thumb {
        background-color: #888;
        border-radius: 4px;
    }
    .generic-listbox::-webkit-scrollbar-thumb:hover {
        background-color: #555;
    }
`;
document.head.appendChild(style);

// Export the class
export default GenericListbox;
