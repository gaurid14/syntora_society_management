// Function to toggle between editable and non-editable states
function toggleEdit(button) {
    const row = button.closest('tr');
    const inputs = row.querySelectorAll('input');
    const selects = row.querySelectorAll('select');

    inputs.forEach(input => {
        if (!input.hasAttribute('data-mygate_no')) {
            if (input.hasAttribute('readonly')) {
                input.removeAttribute('readonly');
                input.style.backgroundColor = '#ffffff';
            } else {
                input.setAttribute('readonly', true);
                input.style.backgroundColor = '#f7f7f7';
            }
        }
    });

    selects.forEach(select => {
        if (select.hasAttribute('disabled')) {
            select.removeAttribute('disabled');
            select.style.backgroundColor = '#ffffff';
        } else {
            select.setAttribute('disabled', true);
            select.style.backgroundColor = '#f7f7f7';
        }
    });

    button.textContent = button.textContent === 'Save' ? 'Edit' : 'Save';

    if (button.textContent === 'Save') {
        const residentData = {
            room_no: row.querySelector('input[placeholder="Room No"]').value,
            bhk: row.querySelector('input[placeholder="BHK"]').value,
            mr_ms: row.querySelector('select').value,
            name: row.querySelector('input[placeholder="Name"]').value,
            gender: row.querySelector('input[placeholder="Gender"]').value,
            age: row.querySelector('input[placeholder="Age"]').value,
            email: row.querySelector('input[placeholder="Email"]').value,
            contactNo: row.querySelector('input[placeholder="Contact No"]').value,
            status: row.querySelector('select').value
        };

        console.log("Updating resident: ", residentData);

        fetch('/api/update_resident', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(residentData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('Resident updated successfully!');
                button.textContent = 'Edit';
                inputs.forEach(input => input.setAttribute('readonly', true));
                selects.forEach(select => select.setAttribute('disabled', true));
            } else {
                alert('Error updating resident: ' + data.error);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error updating resident: ' + error);
        });
    }
}

    // Function to add a new row (resident) to the table
    document.getElementById('addResidentBtn').addEventListener('click', function() {
        const tableBody = document.getElementById('residentTableBody');
        const newRow = document.createElement('tr');

        newRow.innerHTML = `
            <td><input type="checkbox"></td>
            <td>${tableBody.rows.length + 1}</td>
            <td><input type="text" placeholder="MyGate No" readonly></td>
            <td><input type="text" placeholder="Room No"></td>
            <td><input type="text" placeholder="BHK"></td>
            <td>
                <select>
                    <option>Mr.</option>
                    <option>Ms.</option>
                </select>
            </td>
            <td><input type="text" placeholder="Name"></td>
            <td><input type="text" placeholder="Gender"></td>
            <td><input type="number" placeholder="Age"></td>
            <td><input type="email" placeholder="Email"></td>
            <td><input type="text" placeholder="Contact No"></td>
            <td>
                <select>
                    <option>Paid</option>
                    <option>Unpaid</option>
                </select>
            </td>
            <td>
                <button class="toggle-btn" onclick="toggleAdd(this)">Save</button>
            </td>
        `;

        tableBody.appendChild(newRow);
        updateSerialNumbers(); // Update serial numbers after adding a row
    });

function toggleAdd(button) {
    const row = button.closest('tr');
    const inputs = row.querySelectorAll('input');
    const selects = row.querySelectorAll('select');

    const residentData = {
        room_no: row.querySelector('input[placeholder="Room No"]').value,
        bhk: row.querySelector('input[placeholder="BHK"]').value,
        mr_ms: row.querySelector('select').value,
        name: row.querySelector('input[placeholder="Name"]').value,
        gender: row.querySelector('input[placeholder="Gender"]').value,
        age: row.querySelector('input[placeholder="Age"]').value,
        email: row.querySelector('input[placeholder="Email"]').value,
        contactNo: row.querySelector('input[placeholder="Contact No"]').value,
        status: row.querySelector('select').value
    };

    console.log("Adding new resident: ", residentData);

    if (!residentData.room_no || !residentData.name || !residentData.age || !residentData.contactNo || !residentData.email) {
        alert("Please fill out all required fields!");
        return;
    }

    fetch('/api/add_resident', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(residentData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('Resident added successfully!');
            button.textContent = 'Edit';
        } else {
            alert('Error adding resident: ' + data.error);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error adding resident: ' + error);
    });
}


    // Function to update serial numbers
    function updateSerialNumbers() {
        const rows = document.querySelectorAll('#residentTableBody tr');
        rows.forEach((row, index) => {
            row.cells[1].textContent = index + 1; // Update the serial number
        });
    }

    // Function to delete selected residents
    document.getElementById('deleteResidentBtn').addEventListener('click', function() {
        const rows = document.querySelectorAll('#residentTableBody tr');
        const selectedResidents = Array.from(rows).filter(row => row.querySelector('input[type="checkbox"]').checked);

        selectedResidents.forEach(row => {
            const mygate_no = row.querySelector('input[data-mygate_no="true"]').value;

            // Send the delete request to the backend
            fetch(`/api/delete_resident/${mygate_no}`, {
                method: 'DELETE',
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Resident deleted successfully!');
                    row.remove(); // Remove the row from the table
                    updateSerialNumbers(); // Update serial numbers after deletion
                } else {
                    alert('Error deleting resident: ' + data.error);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error deleting resident: ' + error);
            });
        });
    });