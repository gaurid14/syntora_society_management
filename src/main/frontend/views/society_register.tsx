import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../themes/my-theme/auth.css';

interface Society {
  sid: string;
  name: string;
}

export function SocietyRegistration() {
  const [isNewSociety, setIsNewSociety] = useState(true);
  const [societies, setSocieties] = useState<Society[]>([]);
  const [formData, setFormData] = useState({
    name: '',
    street: '',
    landmark: '',
    locality: '',
    city: '',
    state: '',
    pincode: '',
    country: '',
    societyId: '' // Add this field for existing society selection
  });
  const navigate = useNavigate();

  useEffect(() => {
    if (!isNewSociety) {
      fetch('/api/societies/existing')
        .then(response => response.json())
        .then(data => setSocieties(data))
        .catch(error => console.error('Error fetching societies:', error));
    }
  }, [isNewSociety]);

  const handleAdminLoginClick = () => {
    navigate('/admin_register');
  }

//   const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
//     const { name, value } = e.target;
//     setFormData(prevState => ({ ...prevState, [name]: value }));
//   }

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
      const { name, value } = e.target;
      console.log("Input changed:", name, value);  // Add this to check what's being captured
      setFormData((prevState) => ({ ...prevState, [name]: value }));

      if (name === "existingSociety") {
      console.log("Selected society ID: ", value);
       // Update societyId in the form data
        setFormData((prevState) => ({ ...prevState, societyId: value }));
        // Send the selected societyId to the backend to store in session
        fetch("/api/societies/setSocietyId", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ societyId: value }),
        })
          .then((response) => response.json())
          .then((data) => {
            console.log("Society ID set in session:", data);
          })
          .catch((error) => {
            console.error("Error setting societyId in session:", error);
          });
      }
    };

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    fetch('/api/societies/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(formData),
    })
    .then(response => {
      if (response.ok) {
        alert('Society registered successfully');
        navigate('/admin_register'); // Redirect to admin register on success
        return; // Ensure all paths return
      } else {
        return response.text().then(text => { throw new Error(text); });
      }
    })
    .catch(error => alert('Error submitting form: ' + error.message));
  }

  return (
    <div className="auth-container">
      <div className="society-registration-container">
        <h2>Society Registration</h2>

        <div className="society-registration-sidebar">
          <button onClick={() => setIsNewSociety(true)}>New Society</button>
          <button onClick={() => setIsNewSociety(false)}>Existing Society</button>
        </div>

        {isNewSociety ? (
          <form onSubmit={handleSubmit} noValidate>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Society Name"
              required
            />
            <input
              type="text"
              name="street"
              value={formData.street}
              onChange={handleChange}
              placeholder="Street"
              required
            />
            <input
              type="text"
              name="landmark"
              value={formData.landmark}
              onChange={handleChange}
              placeholder="Landmark"
            />
            <input
              type="text"
              name="locality"
              value={formData.locality}
              onChange={handleChange}
              placeholder="Locality Eg. Dadar"
              required
              pattern="[a-zA-Z\s]+"
              title="Locality must contain only letters and spaces"
            />
            <input
              type="text"
              name="city"
              value={formData.city}
              onChange={handleChange}
              placeholder="City"
              required
              pattern="[a-zA-Z\s]+"
              title="City must contain only letters and spaces"
            />
            <input
              type="text"
              name="state"
              value={formData.state}
              onChange={handleChange}
              placeholder="State"
              required
              pattern="[a-zA-Z\s]+"
              title="State must contain only letters and spaces"
            />
            <input
              type="text"
              name="pincode"
              value={formData.pincode}
              onChange={handleChange}
              placeholder="Pincode"
              required
              pattern="\d{6}"
              title="Pincode must be a 6-digit number"
            />
            <input
              type="text"
              name="country"
              value={formData.country}
              onChange={handleChange}
              placeholder="Country"
              required
              pattern="[a-zA-Z\s]+"
              title="Country must contain only letters and spaces"
            />

            <button type="submit" title="Submit the registration form">Submit</button>
          </form>
        ) : (
          <>
            <select name="existingSociety" onChange={handleChange} required>
              <option value="" disabled>Select Existing Society</option>
              {societies.map(society => (
                <option key={society.sid} value={society.sid}>
                  {society.name}
                </option>
              ))}
            </select>

            <button type="button" title="Only admin can Register" onClick={handleAdminLoginClick}>
              Admin Registration
            </button>
          </>
        )}
      </div>
    </div>
  );
}

export default SocietyRegistration;
