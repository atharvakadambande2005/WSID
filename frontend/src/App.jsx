
import { useState } from 'react';

function App() {
  const [problem, setProblem] = useState('');
  const [submittedProblem, setSubmittedProblem] = useState('');
  const [serverMessage, setServerMessage] = useState('');
  const [loading, setLoading] = useState(false);

  async function checkBackend() {
    setLoading(true);
    setServerMessage('');

    try {
      const response = await fetch('http://localhost:8080/api/welcome');

      if (!response.ok) {
        throw new Error('Backend request failed');
      }

      setServerMessage(await response.text());
    } catch (error) {
      setServerMessage('Could not connect to the backend.');
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit() {
    if (!problem.trim()) return;

    setLoading(true);
    setServerMessage('');
    setSubmittedProblem('');

    try {
      const response = await fetch('http://localhost:8080/api/problem', {
        method: 'POST',
        headers: {
          'Content-Type': 'text/plain',
        },
        body: problem,
      });

      if (!response.ok) {
        throw new Error('Problem submission failed');
      }

      const message = await response.text();
      setSubmittedProblem(problem);
      setServerMessage(message);
    } catch (error) {
      setServerMessage(
        'Could not submit your problem. Check whether Spring Boot is running.'
      );
    } finally {
      setLoading(false);
    }
  }

  function handleClear() {
    setProblem('');
    setSubmittedProblem('');
    setServerMessage('');
  }

  return (
    <div className="container-fluid min-vh-100 py-5 bg-light">
      <div className="text-center mt-5">
        <h1 className="display-4 fw-bold">What Should I Do?</h1>

        <p className="lead text-secondary mt-3">
          Tell us what's on your mind. Let's figure it out together.
        </p>
      </div>

      <div className="row justify-content-center mt-5">
        <div className="col-12 col-md-8 col-lg-7">
          <div className="card shadow border-0 rounded-4 p-4">
            <label className="form-label fw-semibold">
              What's your situation?
            </label>

            <p className="text-secondary small">
              Share as much detail as you like. We'll help you think through your options.
            </p>

            <textarea
              className="form-control"
              rows="5"
              placeholder="Describe your problem or decision here..."
              value={problem}
              onChange={(event) => setProblem(event.target.value)}
            />

            <button
              className="btn btn-lg w-100 mt-3 wsid-button"
              disabled={!problem.trim() || loading}
              onClick={handleSubmit}
            >
              {loading ? 'Sending...' : 'Help Me Decide'}
            </button>

            <button
              className="btn btn-outline-secondary w-100 mt-2"
              onClick={handleClear}
              disabled={loading || (!problem && !submittedProblem && !serverMessage)}
            >
              Clear
            </button>

            {submittedProblem && (
              <div className="alert alert-info mt-4">
                <h5>Your situation</h5>
                <p className="mb-0">{submittedProblem}</p>
              </div>
            )}

            {serverMessage && (
              <div className="alert alert-success mt-3 mb-0">
                {serverMessage}
              </div>
            )}

            <hr className="my-4" />

            <button
              className="btn btn-outline-primary w-100"
              onClick={checkBackend}
              disabled={loading}
            >
              {loading ? 'Connecting...' : 'Test Backend Connection'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
